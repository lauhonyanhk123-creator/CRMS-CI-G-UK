#!/bin/bash
# CRMS CI G UK - Kubernetes Deployment Script
# Usage: ./deploy.sh [environment] [namespace]

set -e

ENVIRONMENT=${1:-staging}
NAMESPACE=${2:-crms}
KUBECONFIG_PATH=${KUBECONFIG_PATH:-~/.kube/config}

echo "Deploying CRMS CI G UK to ${ENVIRONMENT} environment..."

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(staging|production)$ ]]; then
    echo "ERROR: Invalid environment. Must be 'staging' or 'production'"
    exit 1
fi

# Check kubectl
if ! command -v kubectl &> /dev/null; then
    echo "ERROR: kubectl not installed"
    exit 1
fi

# Check context
if ! kubectl config current-context &> /dev/null; then
    echo "WARNING: No active kubectl context"
fi

# Apply namespace
echo "Applying namespace..."
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/namespace.yaml" || true

# Apply secrets (requires manual creation in production)
echo "Applying secrets..."
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/secrets.yaml" || echo "Skipping secrets - may need manual creation"

# Apply other resources
echo "Applying ConfigMap..."
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/configmap.yaml"

echo "Applying infrastructure..."
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/postgres.yaml"
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/minio.yaml"

echo "Waiting for infrastructure to be ready..."
kubectl wait --for=condition=available deployment/postgres -n crms-infra --timeout=300s || true
kubectl wait --for=condition=available deployment/minio -n crms-infra --timeout=300s || true

echo "Applying application resources..."
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/backend.yaml"
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/frontend.yaml"
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/nginx.yaml"
kubectl apply -f "kubernetes/overlays/${ENVIRONMENT}/ingress.yaml"

# Wait for rollout
echo "Waiting for deployments..."
kubectl rollout status deployment/crms-backend -n crms --timeout=600s
kubectl rollout status deployment/crms-frontend -n crms --timeout=300s
kubectl rollout status deployment/crms-nginx -n crms --timeout=300s

# Show status
echo ""
echo "Deployment Status:"
kubectl get pods -n crms
kubectl get services -n crms

echo ""
echo "Deployment to ${ENVIRONMENT} completed!"
