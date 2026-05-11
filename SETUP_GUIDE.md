# CRMS CI G UK - Complete Setup Guide

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Quick Start (Docker)](#quick-start-docker)
3. [Development Setup](#development-setup)
4. [Post-Installation Setup](#post-installation-setup)
5. [First Login](#first-login)
6. [Service URLs](#service-urls)
7. [Troubleshooting](#troubleshooting)
8. [Next Steps](#next-steps)

---

## System Requirements

### Minimum Hardware

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **RAM** | 8 GB | 16 GB |
| **CPU** | 2 cores | 4+ cores |
| **Storage** | 50 GB | 100+ GB |
| **OS** | Ubuntu 20.04+, macOS 12+, Windows 10+ | Ubuntu 22.04 LTS |

### Required Software

- **Docker** (v20.10+)
- **Docker Compose** (v2.0+)
- **Git** (for cloning repository)

### Verify Docker Installation

```bash
# Check Docker version
docker --version
# Expected: Docker version 20.10.x or higher

# Check Docker Compose version
docker compose version
# Expected: Docker Compose version v2.x.x or higher

# Verify Docker is running
docker ps
# Expected: Should show headers without errors
```

---

## Quick Start (Docker)

### Option 1: Production Stack (Recommended for Demo)

```bash
# 1. Clone the repository
git clone https://github.com/lauhonyanhk123-creator/CRMS-CI-G-UK.git
cd CRMS-CI-G-UK

# 2. Navigate to Docker directory
cd docker

# 3. Copy environment file
cp .env.example .env

# 4. Edit .env with your settings (at minimum, set strong passwords)
nano .env  # or use your preferred editor

# 5. Start all services
docker compose up -d

# 6. Wait for services to start (2-3 minutes)
docker compose logs -f

# 7. Verify services are running
docker compose ps
```

**Expected Output:**
```
NAME                COMMAND                  SERVICE   STATUS
crms-backend        "/crms-entrypoint…"     backend   Up (healthy)
crms-frontend       "/docker-entrypoint…"   frontend  Up (healthy)
crms-minio          "/usr/bin/minio serv…"   minio     Up (healthy)
crms-nginx          "/docker-entrypoint…"   nginx     Up (healthy)
crms-postgres       "docker-entrypoint.sh"   postgres  Up (healthy)
crms-redis          "docker-entrypoint.sh"   redis     Up (healthy)
```

### Option 2: Development Stack

```bash
# 1. Navigate to Docker directory
cd docker

# 2. Start development stack (includes MailHog, more verbose logging)
docker compose -f docker-compose.dev.yml up -d

# 3. Monitor startup
docker compose -f docker-compose.dev.yml logs -f
```

---

## Development Setup

### Prerequisites

Ensure you have the following installed:

- Node.js 20.x (LTS)
- Java 21 (JDK)
- Maven 3.9+
- PostgreSQL 16 client tools
- pnpm 9+

### Frontend Development

```bash
# 1. Navigate to frontend directory
cd frontend

# 2. Install dependencies
pnpm install

# 3. Start development server
pnpm dev

# Frontend will be available at http://localhost:5173
# It will connect to backend at http://localhost:8080
```

### Backend Development

```bash
# 1. Navigate to backend directory
cd backend

# 2. Build the application
./mvnw clean package -DskipTests

# 3. Run the application
./mvnw spring-boot:run

# Or run the JAR directly:
java -jar target/crms-backend-1.0.0.jar
```

### Database Setup

```bash
# 1. Connect to PostgreSQL
psql -h localhost -p 5432 -U crms -d crms

# 2. Verify tables exist
\dt

# 3. Check Flyway migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## Post-Installation Setup

### 1. Create Test Data (Optional)

The system comes with sample data. To add more:

```bash
# Connect to MinIO console at http://localhost:9001
# Create buckets for documents and images

# Upload test documents via the CRMS web interface
```

### 2. Configure External Integrations (Optional)

#### HMRC CIS Integration

```bash
# In .env file, add your HMRC credentials:
HMRC_CLIENT_ID=your_client_id
HMRC_CLIENT_SECRET=your_client_secret
HMRC_API_MODE=sandbox  # or 'live' for production
```

#### Companies House API

```bash
# Add your API key in .env:
COMPANIES_HOUSE_API_KEY=your_api_key
```

#### CSCS Smart Check

```bash
# Add CSCS credentials in .env:
CSCS_CLIENT_ID=your_client_id
CSCS_CLIENT_SECRET=your_client_secret
```

### 3. Email Configuration (Development)

The system uses MailHog for development. Access it at:

```
http://localhost:8025
```

All outgoing emails will be captured here instead of being sent.

### 4. SSL/HTTPS Setup (Production)

For production, configure HTTPS in `docker/nginx.conf`:

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    # ... rest of configuration
}
```

---

## First Login

### Default Credentials

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin@crms.local | Admin123! | System Administrator | Full access |
| ops@crms.local | OpsDirector123! | Operations Director | Operations |
| contracts@crms.local | ContractsMgr123! | Contracts Manager | Contracts |
| qs@crms.local | QS123! | Senior QS | Estimating |
| site@crms.local | SiteAgent123! | Site Agent | Site operations |
| engineer@crms.local | Engineer123! | Site Engineer | Engineering |
| plant@crms.local | PlantMgr123! | Plant Manager | Plant |
| buyer@crms.local | Buyer123! | Buyer | Procurement |
| finance@crms.local | Finance123! | Finance Manager | Finance |
| estimator@crms.local | Estimator123! | Estimator | Estimating |

**⚠️ IMPORTANT:** Change all passwords on first login!

### Login Steps

1. **Access the Application**
   ```
   http://localhost:5173
   ```

2. **Enter Credentials**
   - Username: `admin@crms.local`
   - Password: `Admin123!`

3. **Complete 2FA Setup (First Login)**
   - Set up TOTP authenticator app (Google Authenticator, Authy, etc.)
   - Scan QR code
   - Enter 6-digit code to verify

4. **Change Password**
   - Navigate to User Management
   - Change default password to a strong password

---

## Service URLs

### Development Stack

| Service | URL | Default Credentials |
|---------|-----|-------------------|
| **Frontend** | http://localhost:5173 | - |
| **Backend API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **MinIO Console** | http://localhost:9001 | dev_user / dev_password |
| **MailHog** | http://localhost:8025 | - |
| **PostgreSQL** | localhost:5432 | crms / dev_password |
| **Redis** | localhost:6379 | (no auth in dev) |

### Production Stack

| Service | URL | Notes |
|---------|-----|-------|
| **Frontend** | https://your-domain.com | Via Nginx |
| **Backend API** | https://your-domain.com/api | Via Nginx |
| **MinIO Console** | https://your-domain.com:9001 | Direct access |
| **API Documentation** | https://your-domain.com/swagger-ui.html | Via Nginx |

---

## Troubleshooting

### Services Won't Start

#### Check Port Conflicts

```bash
# Check if ports are in use
sudo lsof -i :5173  # Frontend
sudo lsof -i :8080  # Backend
sudo lsof -i :5432  # PostgreSQL
sudo lsof -i :6379  # Redis
sudo lsof -i :9000  # MinIO

# Kill conflicting processes if needed
sudo lsof -ti :8080 | xargs sudo kill -9
```

#### Check Docker Logs

```bash
# View logs for specific service
docker compose logs backend
docker compose logs frontend
docker compose logs postgres

# Follow logs in real-time
docker compose logs -f
```

#### Check Resource Availability

```bash
# Check disk space
df -h

# Check memory
free -h

# Increase Docker resources in Docker Desktop settings
```

### Database Connection Issues

#### Verify PostgreSQL is Running

```bash
# Check PostgreSQL container
docker compose ps postgres

# Test connection
docker compose exec postgres psql -U crms -d crms -c "SELECT 1;"

# View PostgreSQL logs
docker compose logs postgres
```

#### Check Database Credentials

```bash
# Verify .env file has correct credentials
cat docker/.env | grep POSTGRES
```

### Backend Won't Start

#### Check Java Memory

```bash
# Edit docker-compose.yml and increase memory:
environment:
  JAVA_OPTS: "-Xms512m -Xmx2g"
```

#### Verify Maven Build

```bash
cd backend
./mvnw clean package
java -jar target/crms-backend-1.0.0.jar
```

### Frontend Issues

#### Clear Node Modules

```bash
cd frontend
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

#### Check API Connection

```bash
# Backend should be accessible
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

### MinIO Issues

#### Access MinIO Console

```bash
# Open browser to http://localhost:9001
# Login with: dev_user / dev_password

# Check if buckets exist
docker compose exec minio mc ls local/
```

### Redis Issues

```bash
# Test Redis connection
docker compose exec redis redis-cli ping
# Should return: PONG
```

### Network Issues

#### Check Docker Network

```bash
# View networks
docker network ls

# Inspect CRMS network
docker network inspect crms-ci-g-uk_crms-network
```

---

## Next Steps

### 1. Explore the Features

Once logged in, explore:

- **Dashboard** - Overview of all operations
- **Tenders** - Sales pipeline and BoQ management
- **Contracts** - Contract and commercial control
- **Subcontractors** - CIS and supply chain management
- **Operatives** - Workforce and competence tracking
- **Plant** - Plant register and maintenance
- **Materials** - Procurement and delivery
- **Health & Safety** - CDM and RAMS management
- **Quality** - ITPs and inspections
- **Adoption** - Section 38/278/104 workflows
- **Reports** - CVR, cashflow, retention schedules

### 2. Configure System Settings

- Set up company profile
- Configure email notifications
- Set default values for contracts
- Configure tax rates (VAT, retention)
- Set up user roles and permissions

### 3. Test Integrations

- Test HMRC CIS submission (sandbox mode)
- Verify Companies House API connection
- Test CSCS card validation
- Set up email notifications

### 4. Production Deployment

For production deployment:

1. Configure strong passwords in `.env`
2. Set up SSL certificates
3. Configure automated backups
4. Set up monitoring and alerting
5. Review security settings
6. Plan disaster recovery

### 5. Training

- Train team members on system usage
- Document custom workflows
- Create SOPs for critical processes

---

## Useful Commands

```bash
# Stop all services
docker compose down

# Restart all services
docker compose restart

# View resource usage
docker stats

# Clean up unused resources
docker system prune -a

# Backup database
docker compose exec postgres pg_dump -U crms crms > backup.sql

# Restore database
docker compose exec -T postgres psql -U crms crms < backup.sql

# Update to latest version
git pull origin master
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

## Getting Help

### Documentation

- [README.md](README.md) - Project overview
- [API Documentation](docs/api.md) - REST API reference
- [Installation Guide](docs/installation.md) - Detailed installation
- [Configuration Reference](docs/configuration.md) - Configuration options

### Support

- Check logs: `docker compose logs -f`
- Review error messages
- Consult Swagger API docs at http://localhost:8080/swagger-ui.html

### Common Issues

1. **Out of memory**: Increase Docker Desktop memory allocation
2. **Port conflicts**: Change ports in `docker-compose.yml`
3. **Database migration fails**: Check PostgreSQL logs
4. **Frontend can't connect to backend**: Verify CORS settings

---

**Happy Testing! 🚀**

For more information, see the main [README.md](README.md) or explore the API documentation.
