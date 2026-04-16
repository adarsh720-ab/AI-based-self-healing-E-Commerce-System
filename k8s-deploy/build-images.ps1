param(
    [string]$Tag = "latest",
    [string]$Registry = "ecommerce",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Assert-LastExitCode([string]$Action) {
    if ($LASTEXITCODE -ne 0) {
        throw "$Action failed with exit code $LASTEXITCODE"
    }
}

Write-Host "======================================================"
Write-Host " Building all Docker images - tag: $Tag"
Write-Host "======================================================"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$dockerfilesDir = Join-Path $root "k8s-deploy\dockerfiles"
$mlDockerfile = Join-Path $root "ml-service\Dockerfile"

if (-not (Test-Path $dockerfilesDir)) {
    throw "Dockerfiles folder not found: $dockerfilesDir"
}

if (-not (Test-Path $mlDockerfile)) {
    throw "ML Dockerfile not found: $mlDockerfile"
}

$services = @(
    "auth-service",
    "user-service",
    "api-gateway",
    "product-service",
    "inventory-service",
    "order-service",
    "payment-service",
    "notification-service",
    "delivery-service",
    "ai-service",
    "action-engine"
)

foreach ($service in $services) {
    $dockerfile = "k8s-deploy/dockerfiles/$service.Dockerfile"
    $image = "$Registry/$service`:$Tag"

    Write-Host ""
    Write-Host ">>> Building $service ..."

    if ($DryRun) {
        Write-Host "docker build -f $dockerfile -t $image ."
    }
    else {
        docker build -f $dockerfile -t $image .
        Assert-LastExitCode "Build for $service"
    }

    Write-Host ">>> $service built successfully"
}

$mlImage = "$Registry/ml-service`:$Tag"
Write-Host ""
Write-Host ">>> Building ml-service ..."

if ($DryRun) {
    Write-Host "docker build -f ml-service/Dockerfile -t $mlImage ./ml-service"
}
else {
    docker build -f ml-service/Dockerfile -t $mlImage ./ml-service
    Assert-LastExitCode "Build for ml-service"
}

Write-Host ">>> ml-service built successfully"
Write-Host ""
Write-Host "======================================================"
Write-Host " All images built successfully!"
Write-Host "======================================================"
Write-Host ""

if ($DryRun) {
    Write-Host "Dry run complete."
}
else {
    docker image ls --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" | Select-String "^$Registry/"
    Assert-LastExitCode "Listing images"
}

