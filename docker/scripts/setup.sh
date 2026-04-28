#!/bin/bash
# CRMS CI G UK - Setup Script for Linux/macOS
# Self-hosted deployment on NAS, Linux mini-PC, or Windows Server

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="crms-ci-g-uk"
DATA_DIR="./crms-data"
DOCKER_COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CRMS CI G UK Setup Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check for Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed.${NC}"
    echo "Please install Docker first: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed.${NC}"
    echo "Please install Docker Compose first: https://docs.docker.com/compose/install/"
    exit 1
fi

# Detect docker compose command
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

echo -e "${GREEN}Docker found:${NC} $(docker --version)"
echo -e "${GREEN}Docker Compose found:${NC} $(${DOCKER_COMPOSE} version 2>/dev/null || echo 'legacy')"
echo ""

# Create directories
echo -e "${YELLOW}Creating directories...${NC}"
mkdir -p "${DATA_DIR}/postgres"
mkdir -p "${DATA_DIR}/minio"
mkdir -p "${DATA_DIR}/uploads"
mkdir -p "${DATA_DIR}/backups"
mkdir -p "${DATA_DIR}/logs/nginx"
mkdir -p "./nginx/ssl"
mkdir -p "./scripts"

echo "Directories created:"
echo "  - ${DATA_DIR}/postgres"
echo "  - ${DATA_DIR}/minio"
echo "  - ${DATA_DIR}/uploads"
echo "  - ${DATA_DIR}/backups"
echo "  - ${DATA_DIR}/logs/nginx"
echo ""

# Generate secure passwords
echo -e "${YELLOW}Generating secure secrets...${NC}"
POSTGRES_PASSWORD=$(openssl rand -base64 32)
MINIO_ACCESS_KEY=$(openssl rand -hex 16)
MINIO_SECRET_KEY=$(openssl rand -base64 32)
APP_SECRET_KEY=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -hex 64)

# Create .env file
cat > "${ENV_FILE}" << EOF
# CRMS CI G UK Environment Configuration
# Generated on $(date)

# =============================================================================
# DATABASE
# =============================================================================
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}

# =============================================================================
# MINIO OBJECT STORAGE
# =============================================================================
MINIO_ROOT_USER=${MINIO_ACCESS_KEY}
MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY}

# =============================================================================
# APPLICATION SECURITY
# =============================================================================
APP_SECRET_KEY=${APP_SECRET_KEY}
JWT_SECRET=${JWT_SECRET}

# =============================================================================
# DEPLOYMENT
# =============================================================================
REGISTRY=quay.io/crms-ci-g-uk
VERSION=1.0.0
HTTP_PORT=80
HTTPS_PORT=443
DATA_DIR=${DATA_DIR}
SPRING_PROFILES=prod
BACKUP_RETENTION_DAYS=30
HMRC_API_MODE=sandbox
EOF

echo -e "${GREEN}.env file created with secure secrets${NC}"
echo ""

# Set permissions
chmod 600 "${ENV_FILE}"
chmod +x "${DOCKER_COMPOSE_FILE%.*}" 2>/dev/null || true

# Start services
echo -e "${YELLOW}Starting CRMS CI G UK services...${NC}"
echo ""

${DOCKER_COMPOSE} up -d postgres minio

# Wait for database
echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
for i in {1..30}; do
    if docker exec crms-postgres pg_isready -U crms -d crms &>/dev/null; then
        echo -e "${GREEN}PostgreSQL is ready!${NC}"
        break
    fi
    echo "  Waiting... ($i/30)"
    sleep 2
done

# Wait for MinIO
echo -e "${YELLOW}Waiting for MinIO to be ready...${NC}"
for i in {1..30}; do
    if docker exec crms-minio mc ready local &>/dev/null; then
        echo -e "${GREEN}MinIO is ready!${NC}"
        break
    fi
    echo "  Waiting... ($i/30)"
    sleep 2
done

# Initialize MinIO bucket
echo -e "${YELLOW}Initializing MinIO bucket...${NC}"
docker run --rm --network crms-ci-g-uk_crms-network \
    minio/mc:latest \
    /bin/sh -c "
    mc alias set local http://minio:9000 ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY} 2>/dev/null || true;
    mc mb local/crms --ignore-existing 2>/dev/null || true;
    mc anonymous set download local/crms 2>/dev/null || true;
    " || true

# Start remaining services
${DOCKER_COMPOSE} up -d backend frontend nginx

# Wait for backend
echo -e "${YELLOW}Waiting for backend to be ready...${NC}"
for i in {1..60}; do
    if curl -sf http://localhost:8080/actuator/health &>/dev/null; then
        echo -e "${GREEN}Backend is ready!${NC}"
        break
    fi
    echo "  Waiting... ($i/60)"
    sleep 5
done

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}  CRMS CI G UK is now running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Access the application at:"
echo "  - Web UI: http://localhost"
echo "  - API: http://localhost:8080"
echo "  - MinIO Console: http://localhost:9001"
echo ""
echo "Default credentials:"
echo "  - MinIO Access Key: ${MINIO_ACCESS_KEY}"
echo "  - MinIO Secret Key: ${MINIO_SECRET_KEY}"
echo ""
echo -e "${YELLOW}IMPORTANT: ${NC}Store your .env file securely!"
echo "Run 'docker compose logs -f' to view logs"
echo "Run 'docker compose down' to stop services"
echo ""
