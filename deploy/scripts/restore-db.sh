#!/bin/bash
# CRMS CI G UK - Database Restore Script
# Usage: ./restore-db.sh <backup_file>

set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file>"
    echo "Available backups:"
    ls -la ./backups/postgres/crms_backup_*.sql.gz 2>/dev/null || echo "No backups found"
    exit 1
fi

BACKUP_FILE=$1

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

echo "WARNING: This will overwrite the current database!"
echo "Backup file: ${BACKUP_FILE}"
read -p "Continue? (yes/no): " CONFIRM

if [ "${CONFIRM}" != "yes" ]; then
    echo "Restore cancelled"
    exit 0
fi

echo "Starting database restore..."

# Drop and recreate database
psql -h postgres -U crms -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'crms' AND pid <> pg_backend_pid();"
psql -h postgres -U crms -d postgres -c "DROP DATABASE IF EXISTS crms;"
psql -h postgres -U crms -d postgres -c "CREATE DATABASE crms;"

# Restore backup
gunzip -c "${BACKUP_FILE}" | psql -h postgres -U crms -d crms

echo "Database restore completed successfully!"
