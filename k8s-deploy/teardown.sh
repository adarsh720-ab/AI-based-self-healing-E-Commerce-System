#!/bin/bash
# ================================================================
# teardown.sh
# Removes all deployed resources from the cluster.
# Usage: ./teardown.sh
# ================================================================

echo "======================================================"
echo " Tearing down Ecommerce Platform from Kubernetes"
echo "======================================================"

kubectl delete -f k8s/autoscaling/   --ignore-not-found
kubectl delete -f k8s/ingress/       --ignore-not-found
kubectl delete -f k8s/services/      --ignore-not-found
kubectl delete -f k8s/infrastructure/ --ignore-not-found
kubectl delete -f k8s/configmap.yaml --ignore-not-found
kubectl delete -f k8s/secrets.yaml   --ignore-not-found
kubectl delete -f k8s/namespace.yaml --ignore-not-found

echo ""
echo "Teardown complete. PVCs are retained by default."
echo "To delete PVCs too: kubectl delete pvc --all -n ecommerce"
