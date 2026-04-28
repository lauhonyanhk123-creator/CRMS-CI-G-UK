#!/bin/bash
# CRMS CI G UK - Restore Script
# WARNING: This will overwrite current data!

set -e

BACKUP_NAME="${1:-}"
BACKUP_DIR="${DATA_DIR:-./data}/backups"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ -z "${BACKUP_NAME}" ]; then
    echo -e "${RED}Usage: $0 <backup_name>${NC}"
    echo ""
    echo "Available backups:"
    ls -1 "${BACKUP_DIR}"/*.json 2>/dev/null | sed 's/.*backup_//;s/\.json//' || echo "  No backups found"
    exit 1
fi

echo -e "${YELLOW}CRMS CI G UK Restore Script${NC}"
echo -e "${RED}WARNING: This will overwrite current data!${NC}"
echo ""
read -p "Are you sure you want to restore from backup '${BACKUP_NAME}'? (yes/no): " confirm

if [ "${confirm}" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

echo ""
echo "Restore started at: $(date)"
echo ""

# Stop services
echo "Stopping services..."
docker stop crms-backend crms-frontend crms-postgres crms-minio 2>/dev/null || true

# Restore PostgreSQL
if [ -f "${BACKUP_DIR}/postgres_${BACKUP_NAME}.sql.gz" ]; then
    echo "Restoring PostgreSQL database..."
    gunzip < "${BACKUP_DIR}/postgres_${BACKUP_NAME}.sql.gz" | docker exec -i crms-postgres psql -U crms -d crms
else
    echo -e "${RED}PostgreSQL backup not found: postgres_${BACKUP_NAME}.sql.gz${NC}"
fi

# Restore MinIO
if [ -f "${BACKUP_DIR}/minio_${BACKUP_NAME}.tar.gz" ]; then
    echo "Restoring MinIO data..."
    tar -xzf "${BACKUP_DIR}/minio_${BACKUP_NAME}.tar.gz" -C "${DATA_DIR:-./data}"
fi

# Restore uploads
if [ -f "${BACKUP_DIR}/uploads_${BACKUP_NAME}.tar.gz" ]; then
    echo "Restoring uploads..."
    tar -xzf "${BACKUP_DIR}/uploads_${BACKUP_NAME}.tar.gz" -C "${DATA_DIR:-./data}"
fi

# Start services
echo "Starting services..."
docker start crms-postgres crms-minio crms-backend crms-frontend 2>/dev/null || true

echo ""
echo -e "${GREEN}Restore completed successfully!${NC}"
echo ""
echo "Please verify the application is working correctly."
