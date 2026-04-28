# CRMS CI G UK Installation Guide

This guide covers installing CRMS CI G UK on your own hardware using Docker Compose or Kubernetes.

## System Requirements

### Minimum (Yard Tier - up to 15 users)
| Component | Specification |
|-----------|--------------|
| CPU | 4 cores |
| RAM | 16 GB ECC |
| Storage | 2 TB (SSD/NVMe recommended) |
| OS | Ubuntu 22.04 LTS, Synology DSM 7+, Windows Server 2022 |

### Recommended (Site Tier - up to 40 users)
| Component | Specification |
|-----------|--------------|
| CPU | 8 cores |
| RAM | 32 GB ECC |
| Storage | 4 TB (SSD/NVMe) |
| OS | Ubuntu 22.04 LTS, Windows Server 2022 |

### Maximum (Group Tier - up to 100 users)
| Component | Specification |
|-----------|--------------|
| CPU | 16 cores |
| RAM | 64 GB ECC |
| Storage | 8 TB (SSD/NVMe in RAID 1) |
| OS | Ubuntu 22.04 LTS |

## Prerequisites

### Docker Installation

**Ubuntu/Debian:**
```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

**Windows Server:**
```powershell
Install-PackageProvider -Name DockerMsftProvider -Force
Install-WindowsFeature -Name Containers
Restart-Computer
```

### Docker Compose Installation
```bash
sudo apt update
sudo apt install docker-compose-plugin
# OR for standalone:
sudo apt install docker-compose
```

## Installation Steps

### 1. Download and Extract

```bash
# Download the installation package
curl -O https://releases.crms-ci-g-uk.co.uk/crms-ci-g-uk-1.0.0.tar.gz

# Extract
tar -xzf crms-ci-g-uk-1.0.0.tar.gz
cd crms-ci-g-uk
```

### 2. Configure Environment

```bash
cd docker
cp .env.example .env
nano .env  # Edit with your settings
```

Required settings:
```env
POSTGRES_PASSWORD=<secure-password>
MINIO_ROOT_USER=<minio-admin>
MINIO_ROOT_PASSWORD=<secure-password>
APP_SECRET_KEY=<32-char-secret>
JWT_SECRET=<64-char-hex>
```

### 3. Run Setup

```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

This will:
- Create data directories
- Generate secure secrets
- Start all services
- Initialize the database
- Configure MinIO

### 4. Verify Installation

```bash
# Check service status
docker compose ps

# View logs
docker compose logs -f backend

# Run health check
./scripts/healthcheck.sh
```

### 5. Access the Application

| Service | URL |
|---------|-----|
| Web UI | http://your-server |
| API | http://your-server:8080 |
| MinIO Console | http://your-server:9001 |

## Initial Configuration

### First Login

1. Navigate to the web UI
2. Log in with default admin credentials (from setup output)
3. Change the admin password immediately
4. Configure your company details in Settings

### NHS Integration Setup

To enable HMRC CIS integration:

1. Register for HMRC Developer Hub: https://developer.service.hmrc.gov.uk
2. Create an application for the CIS Deductions API
3. Add credentials to `.env`:
```env
HMRC_CLIENT_ID=your-client-id
HMRC_CLIENT_SECRET=your-client-secret
HMRC_API_MODE=production
```

### CSCS Smart Check Setup

Contact CSCS to become an approved IT Partner for Smart Check API access.

## Backup Configuration

Enable automated backups:

```bash
# Add to crontab
crontab -e

# Daily backup at 2 AM
0 2 * * * /path/to/crms-ci-g-uk/docker/scripts/backup.sh
```

## SSL/TLS Setup (Production)

### Option 1: Let's Encrypt

```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Generate certificate
sudo certbot --nginx -d crms.yourdomain.com
```

### Option 2: Self-Signed (Internal Use)

```bash
# Generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/nginx.key \
  -out nginx/ssl/nginx.crt
```

## Firewall Configuration

Open required ports:

```bash
# UFW
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# For remote admin (optional)
sudo ufw allow 2222/tcp  # SSH
```

## Troubleshooting

### Services won't start

```bash
# Check logs
docker compose logs postgres
docker compose logs backend

# Common issues:
# - Port already in use: change HTTP_PORT/HTTPS_PORT
# - Permission denied: sudo chown -R 1000:1000 ./data
```

### Database connection failed

```bash
# Check postgres is running
docker exec crms-postgres pg_isready -U crms

# Check connection
docker exec crms-backend curl localhost:8080/actuator/health
```

### MinIO console not accessible

```bash
# Check MinIO logs
docker compose logs minio

# Recreate bucket
docker exec crms-minio mc mb local/crms
```

## Next Steps

- [Configuration Reference](configuration.md)
- [Backup & Recovery](backup.md)
- [API Documentation](api.md)
