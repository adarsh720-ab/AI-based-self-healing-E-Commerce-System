#!/bin/bash
# ================================================================
# build-images.sh
# Run from your PROJECT ROOT (where pom.xml and all services are)
# Usage: ./k8s-deploy/build-images.shbash k8s-deploy/build-images.sh v1
# ================================================================

set -e

REGISTRY="ecommerce"
TAG="${1:-latest}"

echo "======================================================"
echo " Building all Docker images — tag: $TAG"
echo "======================================================"

# Spring Boot services (built from project root — needs parent pom)
SPRING_SERVICES=(
  "auth-service:8089"
  "user-service:8081"
  "api-gateway:8080"
  "product-service:8082"
  "inventory-service:8085"
  "order-service:8083"
  "payment-service:8084"
  "notification-service:8086"
  "delivery-service:8087"
  "ai-service:8091"
  "action-engine:8092"
)

for entry in "${SPRING_SERVICES[@]}"; do
  SERVICE="${entry%%:*}"
  echo ""
  echo ">>> Building $SERVICE ..."
  docker build \
    -f k8s-deploy/dockerfiles/${SERVICE}.Dockerfile \
    -t ${REGISTRY}/${SERVICE}:${TAG} \
    .
  echo ">>> $SERVICE built successfully"
done

# ML service (has its own Dockerfile inside ml-service/)
echo ""
echo ">>> Building ml-service ..."
docker build \
  -f ml-service/Dockerfile \
  -t ${REGISTRY}/ml-service:${TAG} \
  ./ml-service
echo ">>> ml-service built successfully"

echo ""
echo "======================================================"
echo " All images built successfully!"
echo "======================================================"
echo ""
echo "Images created:"
docker images | grep "^ecommerce" | sort
