# Self-Healing Ecommerce Platform (Microservices)

A production-style, event-driven ecommerce backend built with Spring Boot, Spring Cloud Gateway, Kafka, PostgreSQL, Redis, Elasticsearch, and AI-assisted incident analysis.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.x-6DB33F)
![Architecture](https://img.shields.io/badge/Architecture-Microservices-orange)
![Messaging](https://img.shields.io/badge/Messaging-Kafka-black)
![Deployment](https://img.shields.io/badge/Deployment-Kubernetes-326CE5)

## Quick Links

- [Architecture Deep Dive](docs/ARCHITECTURE.md)
- [Service Map](#service-map)
- [API Examples](#api-examples-via-api-gateway)
- [AI Service Sample Responses](#ai-service-sample-responses)
- [Local Development (Docker Compose)](#local-development-docker-compose)
- [Kubernetes Deployment](#kubernetes-deployment)

## What This Project Is

`ecommerce-platform` is a modular microservices system where business services (auth, users, products, orders, payments, inventory, delivery, notifications) are separated by bounded contexts and connected through synchronous APIs plus asynchronous events.

It also includes a **self-healing pipeline**:
- `ml-service` detects anomalies from service logs/events
- `ai-service` performs LLM-assisted root cause analysis (Ollama/Qwen)
- `action-engine` can execute corrective workflows

## Problem It Solves

Traditional monolith-style ecommerce apps face bottlenecks in scale, fault isolation, and incident response. This project addresses:
- **Scalability:** independent services with HPA support in Kubernetes
- **Resilience:** failure isolation by service and asynchronous Kafka communication
- **Operational visibility:** health endpoints and container-native deployment model
- **Faster incident triage:** AI-generated root-cause suggestions for anomalies

## Core Capabilities

- JWT-based authentication and authorization flow
- API Gateway routing for all public REST APIs
- Product catalog, inventory reservation/release, order lifecycle, payment orchestration
- Notification and delivery domain separation
- AI-assisted anomaly interpretation and suggested remediation
- Docker and Kubernetes deployment-ready structure

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4.x, Spring Cloud Gateway
- **Messaging:** Apache Kafka
- **Datastores:** PostgreSQL, Redis, Elasticsearch
- **AI:** Spring AI + Ollama (Qwen)
- **ML:** Python-based `ml-service`
- **Infra:** Docker Compose, Kubernetes manifests, Ingress, HPA

## Service Map

From `pom.xml`, active modules/services include:
- `api-gateway`
- `auth-service`
- `user-service`
- `product-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `notification-service`
- `delivery-service`
- `ai-service`
- `action-engine`
- `commons`

## API Examples (via API Gateway)

Gateway base URL (local): `http://localhost:8080`

```bash
# Auth: register
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"StrongPass123!"}'

# Auth: login
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"StrongPass123!"}'

# Products: list
curl "http://localhost:8080/api/products?page=0&size=10"

# Products: search
curl "http://localhost:8080/api/products/search?q=phone&page=0&size=10"

# Orders: create (replace <JWT>)
curl -X POST "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":"11111111-1111-1111-1111-111111111111","quantity":1}]}'

# Payments: initiate
curl -X POST "http://localhost:8080/api/payments/initiate" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"22222222-2222-2222-2222-222222222222","amount":499.99,"currency":"USD"}'
```

## AI Service Sample Responses

Below are your provided real examples of `ai-service` LLM output.

### 1) Multiple register request with already registered email

```json
{
  "rootCause": "A unique email address was attempted to be registered again, causing a ConflictException.",
  "suggestedFix": [
    "Review the registration logic to ensure that duplicate emails are not allowed.",
    "Implement a check for existing users with the same email before processing the POST request."
  ],
  "severity": "LOW"
}
```

### 2) Service Down

```json
{
  "rootCause": "The auth-service attempted to communicate with another microservice at http://localhost:8081/internal/users/exists but received a connection refused error.",
  "suggestedFix": [
    "Check if the target service is running and accessible on port 8081.",
    "Verify network configurations and firewall rules to ensure there are no blocking issues.",
    "Review logs of the target microservice for any errors or warnings that might indicate why it's not responding."
  ],
  "severity": "CRITICAL"
}
```

## Local Development (Docker Compose)

Prerequisites:
- Docker Desktop
- Java 21 and Maven (for local non-container builds)

```powershell
Set-Location "C:\Users\adars\OneDrive\Desktop\Self Healing ecommerce platform\ecommerce V2\ecommerce-restructured"
docker compose up -d
docker compose ps
```

Stop stack:

```powershell
docker compose down
```

## Kubernetes Deployment

Kubernetes files are under `k8s-deploy/`.

### Build images (Windows PowerShell)

```powershell
Set-Location "C:\Users\adars\OneDrive\Desktop\Self Healing ecommerce platform\ecommerce V2\ecommerce-restructured"
powershell -NoProfile -ExecutionPolicy Bypass -File ".\k8s-deploy\build-images.ps1"
```

### Apply manifests

```powershell
kubectl apply -f .\k8s-deploy\k8s\namespace.yaml
kubectl apply -f .\k8s-deploy\k8s\secrets.yaml
kubectl apply -f .\k8s-deploy\k8s\configmap.yaml
kubectl apply -f .\k8s-deploy\k8s\infrastructure
kubectl apply -f .\k8s-deploy\k8s\services
kubectl apply -f .\k8s-deploy\k8s\autoscaling
kubectl apply -f .\k8s-deploy\k8s\ingress
```

### Verify rollout

```powershell
kubectl get pods -n ecommerce
kubectl get svc -n ecommerce
kubectl get hpa -n ecommerce
kubectl get events -n ecommerce --sort-by=.metadata.creationTimestamp
```

## AI/Ollama Runtime Note

`ai-service` uses Spring AI Ollama integration and needs a reachable Ollama endpoint.

- If Ollama runs on host machine, set reachable base URL for cluster/pods.
- In your current config, `ai-service` reads `OLLAMA_BASE_URL` in `ai-service/src/main/resources/application.yaml`.

Example runtime patch:

```powershell
kubectl set env deployment/ai-service -n ecommerce OLLAMA_BASE_URL=http://host.docker.internal:11434
kubectl rollout restart deployment/ai-service -n ecommerce
kubectl rollout status deployment/ai-service -n ecommerce --timeout=180s
```

## Observability and Troubleshooting

```powershell
kubectl logs deployment/api-gateway -n ecommerce --tail=200
kubectl logs deployment/ai-service -n ecommerce --tail=200
kubectl describe pod <pod-name> -n ecommerce
kubectl get events -n ecommerce --sort-by=.metadata.creationTimestamp
```

Common issues:
- `ImagePullBackOff`: image tag/name mismatch or image not present in local cluster runtime
- `CrashLoopBackOff` on `ai-service`: unreachable Ollama endpoint or startup configuration issue
- HPA target `unknown`: metrics-server not installed/ready

## Security and Configuration

- Replace placeholder credentials in configs before production use.
- Never commit real secrets; prefer Kubernetes Secrets / external secret manager.
- Keep JWT and API keys environment-specific.

## Repository Layout (High-level)

```text
ecommerce-restructured/
  api-gateway/
  auth-service/
  user-service/
  product-service/
  inventory-service/
  order-service/
  payment-service/
  notification-service/
  delivery-service/
  ai-service/
  action-engine/
  ml-service/
  commons/
  k8s-deploy/
```

## Who This Is For

- Backend engineers learning event-driven microservices
- DevOps engineers practicing Docker/Kubernetes deployment patterns
- Teams exploring LLM-assisted incident triage in distributed systems

## License

Add your license in this repository root (for example: MIT, Apache-2.0, or proprietary internal).
