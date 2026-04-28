#!/bin/bash
# CRMS CI G UK - Health Check Script
# Run via cron or monitoring system

set -e

# Configuration
API_URL="${API_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost}"
TIMEOUT=10

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ERRORS=0

check_service() {
    local name="$1"
    local url="$2"
    local health_endpoint="$3"

    if [ -n "${health_endpoint}" ]; then
        url="${url}${health_endpoint}"
    fi

    if curl -sf --max-time "${TIMEOUT}" "${url}" > /dev/null 2>&1; then
        echo -e "${GREEN}[OK]${NC} ${name}"
        return 0
    else
        echo -e "${RED}[FAIL]${NC} ${name}"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

echo "CRMS CI G UK Health Check"
echo "=========================="
echo ""

# Check containers
echo "Container Status:"
for container in crms-postgres crms-minio crms-backend crms-frontend crms-nginx; do
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        status=$(docker inspect --format='{{.State.Health.Status}}' "${container}" 2>/dev/null || echo "running")
        if [ "${status}" = "healthy" ] || [ "${status}" = "running" ]; then
            echo -e "  ${GREEN}[OK]${NC} ${container}"
        else
            echo -e "  ${YELLOW}[WARN]${NC} ${container} (${status})"
        fi
    else
        echo -e "  ${RED}[DOWN]${NC} ${container}"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""

# Check endpoints
echo "Service Endpoints:"
check_service "Backend API" "${API_URL}" "/actuator/health"
check_service "Frontend" "${FRONTEND_URL}" "/health"

echo ""

# Check disk space
echo "Disk Usage:"
df -h "$(pwd)" | tail -1 | awk '{printf "  Root: %s used (%s available)\n", $5, $4}'
df -h "${DATA_DIR:-./data}" 2>/dev/null | tail -1 | awk '{printf "  Data: %s used (%s available)\n", $5, $4}' || true

echo ""

# Check memory
echo "Memory Usage:"
free -h | grep Mem | awk '{printf "  Used: %s / %s\n", $3, $2}'

echo ""

# Exit with appropriate code
if [ ${ERRORS} -gt 0 ]; then
    echo -e "${RED}Health check FAILED: ${ERRORS} issue(s) found${NC}"
    exit 1
else
    echo -e "${GREEN}Health check PASSED: All services healthy${NC}"
    exit 0
fi
