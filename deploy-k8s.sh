#!/bin/bash
set -e

echo "Deploying to Kubernetes..."

echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

echo "Creating configmap and secrets..."
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

echo "Deploying Kafka..."
kubectl apply -f k8s/kafka/

echo "Waiting for Kafka to be ready..."
kubectl wait --for=condition=ready pod -l app=kafka -n estore --timeout=120s || echo "Kafka may not be ready yet, continuing..."

echo "Deploying user-service..."
kubectl apply -f k8s/user-service/

echo "Deploying product-service..."
kubectl apply -f k8s/product-service/

echo "Deploying order-service..."
kubectl apply -f k8s/order-service/

echo "Deploying api-gateway..."
kubectl apply -f k8s/api-gateway/

echo "Done! API Gateway is available at http://localhost:8080"
echo ""
echo "Check deployment status with:"
echo "  kubectl get pods -n estore"
echo ""
echo "View logs with:"
echo "  kubectl logs -f deployment/<service-name> -n estore"
