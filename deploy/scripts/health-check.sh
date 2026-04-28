#!/bin/bash
# CRMS CI G UK - Health Check Script
# Usage: ./health-check.sh [namespace]

set -e

NAMESPACE=${1:-crms}
TIMEOUT=30
INTERVAL=5

check_pod_health() {
    local pod=$1
    local status=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.phase}' 2>/dev/null)
    echo "$status"
}

check_service() {
    local service=$1
    kubectl get service "$service" -n "$NAMESPACE" 2>/dev/null || echo "NOT_FOUND"
}

echo "Running health checks for namespace: $NAMESPACE"
echo "============================================"

# Check pods
echo ""
echo "Pod Status:"
kubectl get pods -n "$NAMESPACE" -o wide

# Check services
echo ""
echo "Service Status:"
kubectl get services -n "$NAMESPACE"

# Check deployments
echo ""
echo "Deployment Status:"
kubectl get deployments -n "$NAMESPACE"

# Check PVCs
echo ""
echo "PVC Status:"
kubectl get pvc -n "$NAMESPACE" 2>/dev/null || echo "No PVCs found"

# Check HPA
echo ""
echo "HPA Status:"
kubectl get hpa -n "$NAMESPACE" 2>/dev/null || echo "No HPAs found"

# Check ingress
echo ""
echo "Ingress Status:"
kubectl get ingress -n "$NAMESPACE" 2>/dev/null || echo "No Ingress found"

echo ""
echo "============================================"
echo "Health check completed"
