# Ecommerce Platform — Docker + Kubernetes Deployment

## Folder structure

```
k8s-deploy/
├── build-images.sh              ← builds all Docker images
├── deploy.sh                    ← applies all K8s manifests in order
├── teardown.sh                  ← removes everything from cluster
├── dockerfiles/
│   ├── auth-service.Dockerfile
│   ├── user-service.Dockerfile
│   ├── api-gateway.Dockerfile
│   ├── product-service.Dockerfile
│   ├── inventory-service.Dockerfile
│   ├── order-service.Dockerfile
│   ├── payment-service.Dockerfile
│   ├── notification-service.Dockerfile
│   ├── delivery-service.Dockerfile
│   ├── ai-service.Dockerfile
│   └── action-engine.Dockerfile
└── k8s/
    ├── namespace.yaml
    ├── secrets.yaml
    ├── configmap.yaml
    ├── infrastructure/
    │   ├── postgres.yaml        ← StatefulSet + PVC + init SQL
    │   ├── redis.yaml           ← StatefulSet + PVC
    │   ├── zookeeper.yaml       ← StatefulSet + PVC
    │   ├── kafka.yaml           ← StatefulSet + PVC
    │   └── elasticsearch.yaml   ← StatefulSet + PVC
    ├── services/
    │   ├── api-gateway.yaml
    │   ├── auth-service.yaml
    │   ├── user-service.yaml
    │   ├── product-service.yaml
    │   ├── backend-services.yaml      ← inventory, order, payment, notification, delivery
    │   └── selfhealing-services.yaml  ← ai-service, action-engine, ml-service
    ├── ingress/
    │   └── ingress.yaml         ← NGINX Ingress → api-gateway
    └── autoscaling/
        └── hpa.yaml             ← HPA for api-gateway, auth, product, order, payment
```

---

## Prerequisites

```bash
# 1. kubeadm cluster must be running
kubectl get nodes

# 2. Install NGINX Ingress Controller (required for ingress.yaml)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/baremetal/deploy.yaml

# 3. Enable metrics-server (required for HPA to work)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## Step 1 — Copy Dockerfiles into your project

Copy each Dockerfile from `dockerfiles/` into the root of your project. All Spring Boot Dockerfiles must be in the **project root** (same level as the parent `pom.xml`) because they need access to the parent `pom.xml` and `commons/` module.

```
your-project-root/
├── pom.xml                          ← parent pom
├── commons/
├── auth-service/
├── ...
├── auth-service.Dockerfile          ← copy here
├── user-service.Dockerfile          ← copy here
├── api-gateway.Dockerfile           ← copy here
├── ... (all other Dockerfiles)
└── k8s-deploy/                      ← this folder
```

---

## Step 2 — Build all Docker images

Run from your **project root**:

```bash
chmod +x k8s-deploy/build-images.sh
./k8s-deploy/build-images.sh
```

With a custom tag:
```bash
./k8s-deploy/build-images.sh v1.0.0
```

---

## Step 3 — Deploy to Kubernetes

```bash
cd k8s-deploy
chmod +x deploy.sh teardown.sh
./deploy.sh
```

---

## Step 4 — Access the platform

Add to your `/etc/hosts`:
```
127.0.0.1  ecommerce.local
```

Then open: `http://ecommerce.local`

---

## Useful commands

```bash
# Watch all pods come up
kubectl get pods -n ecommerce -w

# Check a specific service's logs
kubectl logs -f deployment/api-gateway -n ecommerce
kubectl logs -f deployment/action-engine -n ecommerce
kubectl logs -f deployment/ml-service -n ecommerce

# Check HPA status
kubectl get hpa -n ecommerce

# Scale a service manually
kubectl scale deployment/order-service --replicas=4 -n ecommerce

# Check all services
kubectl get svc -n ecommerce

# Describe a failing pod
kubectl describe pod <pod-name> -n ecommerce

# Get events (useful for debugging)
kubectl get events -n ecommerce --sort-by='.lastTimestamp'

# Teardown everything
./teardown.sh
```

---

## Service accessibility

| Service | Type | Accessible via |
|---|---|---|
| api-gateway | ClusterIP + Ingress | http://ecommerce.local |
| auth-service | ClusterIP | internal only |
| user-service | ClusterIP | internal only |
| product-service | ClusterIP | internal only |
| order-service | ClusterIP | internal only |
| payment-service | ClusterIP | internal only (webhook via api-gateway) |
| inventory-service | ClusterIP | internal only |
| notification-service | ClusterIP | internal only |
| delivery-service | ClusterIP | internal only |
| ai-service | ClusterIP | internal only |
| action-engine | ClusterIP | internal only |
| ml-service | ClusterIP | internal only |

---

## HPA — Auto-scaling

| Service | Min | Max | CPU trigger |
|---|---|---|---|
| api-gateway | 2 | 6 | 60% |
| auth-service | 2 | 4 | 60% |
| product-service | 2 | 5 | 60% |
| order-service | 2 | 5 | 60% |
| payment-service | 2 | 4 | 60% |

---

## Note on Ollama (ai-service)

Ollama runs on your host machine, not inside Kubernetes. The `ai-service` connects to it via `host.docker.internal:11434`. On Linux with kubeadm, replace this with your actual host IP:

```yaml
# In k8s/services/selfhealing-services.yaml
- name: SPRING_AI_OLLAMA_BASE_URL
  value: "http://192.168.1.X:11434"   # replace with your host IP
```
