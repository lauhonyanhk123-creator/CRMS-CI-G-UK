#!/bin/bash
# CRMS CI G UK - Backup Script
# Run manually or via cron

set -e

# Configuration
BACKUP_DIR="${DATA_DIR:-./data}/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="crms_backup_${DATE}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"
CONTAINER_NAME="crms-postgres"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}CRMS CI G UK Backup Script${NC}"
echo "Backup started at: $(date)"
echo ""

# Create backup directory
mkdir -p "${BACKUP_DIR}"

# Stop backend to ensure consistent backup
echo "Pausing backend services..."
docker stop crms-backend crms-frontend 2>/dev/null || true

# Backup PostgreSQL
echo "Backing up PostgreSQL database..."
docker exec "${CONTAINER_NAME}" pg_dump -U crms crms | gzip > "${BACKUP_DIR}/postgres_${BACKUP_NAME}.sql.gz"

# Backup MinIO data
echo "Backing up MinIO data..."
if [ -d "${DATA_DIR:-./data}/minio" ]; then
    tar -czf "${BACKUP_DIR}/minio_${BACKUP_NAME}.tar.gz" -C "${DATA_DIR:-./data}" minio 2>/dev/null || true
fi

# Backup uploads
echo "Backing up uploads..."
if [ -d "${DATA_DIR:-./data}/uploads" ]; then
    tar -czf "${BACKUP_DIR}/uploads_${BACKUP_NAME}.tar.gz" -C "${DATA_DIR:-./data}" uploads 2>/dev/null || true
fi

# Create metadata file
cat > "${BACKUP_DIR}/backup_${BACKUP_NAME}.json" << EOF
{
    "backup_name": "${BACKUP_NAME}",
    "created_at": "$(date -Iseconds)",
    "retention_days": ${RETENTION_DAYS},
    "components": [
        "postgres",
        "minio",
        "uploads"
    ],
    "sizes": {
        "postgres": "$(du -h "${BACKUP_DIR}/postgres_${BACKUP_NAME}.sql.gz" | cut -f1)",
        "minio": "$(du -h "${BACKUP_DIR}/minio_${BACKUP_NAME}.tar.gz" 2>/dev/null | cut -f1 || echo 'N/A')",
        "uploads": "$(du -h "${BACKUP_DIR}/uploads_${BACKUP_NAME}.tar.gz" 2>/dev/null | cut -f1 || echo 'N/A')"
    }
}
EOF

# Restart services
echo "Restarting services..."
docker start crms-backend crms-frontend 2>/dev/null || true

# Cleanup old backups
echo "Cleaning up backups older than ${RETENTION_DAYS} days..."
find "${BACKUP_DIR}" -name "*.sql.gz" -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true
find "${BACKUP_DIR}" -name "*.tar.gz" -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true
find "${BACKUP_DIR}" -name "*.json" -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true

echo ""
echo -e "${GREEN}Backup completed successfully!${NC}"
echo "Backup location: ${BACKUP_DIR}"
echo "Files created:"
ls -lh "${BACKUP_DIR}" | grep "${BACKUP_NAME}" || echo "  (check backup dir for files)"
echo ""
echo "To restore, use: ./scripts/restore.sh ${BACKUP_NAME}"
