#!/bin/bash
# ================================================================
# deploy.sh
# Applies all K8s manifests in the correct dependency order.
# Run from the k8s-deploy/ folder.
# Usage: ./deploy.sh
# ================================================================

set -e

echo "======================================================"
echo " Deploying Ecommerce Platform to Kubernetes"
echo "======================================================"

# 1 — Namespace (must exist before everything else)
echo ""
echo ">>> [1/6] Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# 2 — Secrets and ConfigMap
echo ""
echo ">>> [2/6] Applying secrets and config..."
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml

# 3 — Infrastructure (Postgres, Redis, Zookeeper, Kafka, Elasticsearch)
echo ""
echo ">>> [3/6] Deploying infrastructure..."
kubectl apply -f k8s/infrastructure/postgres.yaml
kubectl apply -f k8s/infrastructure/redis.yaml
kubectl apply -f k8s/infrastructure/zookeeper.yaml
kubectl apply -f k8s/infrastructure/kafka.yaml
kubectl apply -f k8s/infrastructure/elasticsearch.yaml

echo "      Waiting for Postgres to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n ecommerce --timeout=120s

echo "      Waiting for Redis to be ready..."
kubectl wait --for=condition=ready pod -l app=redis -n ecommerce --timeout=60s

echo "      Waiting for Kafka to be ready..."
kubectl wait --for=condition=ready pod -l app=kafka -n ecommerce --timeout=120s

# 4 — Application services
echo ""
echo ">>> [4/6] Deploying application services..."
kubectl apply -f k8s/services/api-gateway.yaml
kubectl apply -f k8s/services/auth-service.yaml
kubectl apply -f k8s/services/user-service.yaml
kubectl apply -f k8s/services/product-service.yaml
kubectl apply -f k8s/services/backend-services.yaml
kubectl apply -f k8s/services/selfhealing-services.yaml

# 5 — Ingress
echo ""
echo ">>> [5/6] Applying ingress..."
kubectl apply -f k8s/ingress/ingress.yaml

# 6 — HPA (autoscaling)
echo ""
echo ">>> [6/6] Applying autoscaling..."
kubectl apply -f k8s/autoscaling/hpa.yaml

echo ""
echo "======================================================"
echo " Deployment complete!"
echo "======================================================"
echo ""
echo "Check pod status:"
echo "  kubectl get pods -n ecommerce"
echo ""
echo "Check services:"
echo "  kubectl get svc -n ecommerce"
echo ""
echo "Access the platform:"
echo "  Add to /etc/hosts:  127.0.0.1  ecommerce.local"
echo "  Then open:          http://ecommerce.local"
echo ""
echo "Watch all pods come up:"
echo "  kubectl get pods -n ecommerce -w"
