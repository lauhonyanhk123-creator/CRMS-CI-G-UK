#!/bin/bash
# CRMS CI G UK - Database Backup Script
# Usage: ./backup-db.sh [output_directory]

set -e

OUTPUT_DIR=${1:-./backups/postgres}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${OUTPUT_DIR}/crms_backup_${TIMESTAMP}.sql.gz"
RETENTION_DAYS=30

echo "Starting database backup..."
echo "Output: ${BACKUP_FILE}"

# Ensure output directory exists
mkdir -p "${OUTPUT_DIR}"

# Perform backup
pg_dump -h postgres -U crms -d crms | gzip > "${BACKUP_FILE}"

# Verify backup
if [ -s "${BACKUP_FILE}" ]; then
    echo "Backup completed successfully: ${BACKUP_FILE}"
    echo "Size: $(du -h ${BACKUP_FILE} | cut -f1)"
else
    echo "ERROR: Backup file is empty!"
    exit 1
fi

# Cleanup old backups
find "${OUTPUT_DIR}" -name "crms_backup_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
echo "Old backups (>${RETENTION_DAYS} days) cleaned up"

# Keep last 10 backups regardless of age
ls -t "${OUTPUT_DIR}"/crms_backup_*.sql.gz | tail -n +11 | xargs -r rm

echo "Backup process completed"
