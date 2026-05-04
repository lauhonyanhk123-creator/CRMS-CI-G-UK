# Installation Guide

CRMS CI G UK is packaged as a set of Docker images. This guide covers hardware sizing, OS preparation, and three deployment paths: Docker Compose (recommended for most sites), Kubernetes/Helm (for larger Group-tier deployments), and bare-metal (advanced).

---

## 1. Hardware Requirements

| Tier | Users | Min RAM | Min Storage | Recommended Hardware |
|------|-------|---------|-------------|----------------------|
| Yard | ≤15 | 16 GB ECC | 4 TB | Synology DS923+, Beelink Ryzen 7 mini-PC |
| Site | ≤40 | 32 GB | 8 TB | Synology DS1522+, Dell PowerEdge T350 |
| Group | ≤100 | 64 GB | 16 TB | HPE ProLiant ML30, Dell PowerEdge T350 |

> Typical data growth: 2–4 GB structured data + 200–500 GB attachments per year.

---

## 2. OS Preparation

### Linux (recommended)
```bash
# Ubuntu 22.04 LTS / Debian 12
sudo apt-get update && sudo apt-get install -y \
    ca-certificates curl gnupg lsb-release

# Install Docker Engine
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose plugin
sudo apt-get install -y docker-compose-plugin
docker compose version   # should print v2.x
```

### Synology NAS
1. Open **Package Center** → search for **Container Manager** → Install
2. Enable SSH: **Control Panel → Terminal & SNMP → Enable SSH**
3. SSH in and use `sudo docker` / `sudo docker compose`

### Windows Server 2022
1. Install Docker Desktop for Windows with WSL2 backend
2. Or install Docker Engine directly in WSL2 (Ubuntu distro recommended)

---

## 3. Clone and Configure

```bash
git clone https://github.com/crms-ci-g-uk/crms-ci-g-uk.git
cd crms-ci-g-uk/docker
cp .env.example .env
```

Edit `.env` and set **all** values marked `CHANGE_ME`:

```bash
# Required — generate strong random values
DB_PASSWORD=<32+ char random string>
JWT_SECRET=<64+ char random string>
ENCRYPTION_KEY=<32-byte hex string>

# MinIO object storage
MINIO_ROOT_USER=crmsadmin
MINIO_ROOT_PASSWORD=<strong password>

# SMTP (optional — needed for email alerts)
SMTP_HOST=smtp.yourprovider.com
SMTP_PORT=587
SMTP_USERNAME=alerts@yourcompany.com
SMTP_PASSWORD=<app password>
```

> **Never commit `.env` to version control.** It is excluded by `.gitignore`.

---

## 4. Start with Docker Compose (Production)

```bash
cd crms-ci-g-uk/docker
docker compose up -d
```

Services started:

| Service | Internal Port | Purpose |
|---------|--------------|---------|
| `postgres` | 5432 | PostgreSQL 16 database |
| `redis` | 6379 | Token blacklist + caching |
| `minio` | 9000/9001 | Object storage (documents, photos) |
| `backend` | 8080 | Spring Boot API |
| `frontend` | 80 | Nginx + Vue 3 SPA |
| `backup` | — | Scheduled pg_basebackup + MinIO sync |

Check all services are healthy:
```bash
docker compose ps
docker compose logs backend --tail 50
```

The application is available at **http://\<server-ip\>** (port 80).
Swagger UI: **http://\<server-ip\>/swagger-ui.html**

---

## 5. Start with Docker Compose (Development)

```bash
cd crms-ci-g-uk/docker
docker compose -f docker-compose.dev.yml up -d

# Frontend hot-reload dev server (separate terminal)
cd ../frontend
pnpm install
pnpm dev
```

| URL | Purpose |
|-----|---------|
| http://localhost:5173 | Frontend dev server (Vite HMR) |
| http://localhost:8080/api/v1 | Backend API |
| http://localhost:8080/swagger-ui.html | API docs |
| http://localhost:9001 | MinIO console |

---

## 6. Deploy on Kubernetes (Helm)

### Prerequisites
- Kubernetes 1.28+ cluster
- Helm 3.14+
- `kubectl` configured with cluster access
- A default `StorageClass` (for PVCs)

### Install

```bash
cd crms-ci-g-uk/docker/helm

# Create namespace
kubectl create namespace crms

# Create secrets (do not store these in values.yaml)
kubectl create secret generic crms-secrets \
  --namespace crms \
  --from-literal=db-password='<DB_PASSWORD>' \
  --from-literal=jwt-secret='<JWT_SECRET>' \
  --from-literal=minio-root-password='<MINIO_PASSWORD>' \
  --from-literal=encryption-key='<ENCRYPTION_KEY>'

# Install the chart
helm install crms . \
  --namespace crms \
  --values values.yaml \
  --set image.tag=1.0.0
```

### Upgrade

```bash
helm upgrade crms . --namespace crms --values values.yaml
```

### Check rollout

```bash
kubectl rollout status deployment/crms-backend -n crms
kubectl get pods -n crms
```

---

## 7. Database Migrations

Flyway runs automatically on backend startup. All 12 schema migrations (V1–V12) are applied in order. On first boot, the schema is created and seeded with:
- 276+ BoQ library items (NRM2/CESMM4-coded)
- 30 RAMS templates
- 10 preconfigured user accounts
- BCIS cost indices
- Casbin RBAC policy

> **Downtime migrations**: V1 creates the full schema. V2–V12 add indices and seed data. Total first-run migration time: under 60 seconds.

---

## 8. First Login

| Username | Password | Role |
|----------|----------|------|
| admin@crms.local | Admin123! | System Administrator |
| ops@crms.local | OpsDirector123! | Operations Director |
| contracts@crms.local | ContractsMgr123! | Contracts Manager |
| qs@crms.local | QS123! | Senior QS |
| site@crms.local | SiteAgent123! | Site Agent |
| engineer@crms.local | Engineer123! | Site Engineer |
| plant@crms.local | PlantMgr123! | Plant Manager |
| buyer@crms.local | Buyer123! | Buyer |
| finance@crms.local | Finance123! | Finance Manager |
| estimator@crms.local | Estimator123! | Estimator |

> **Change all passwords immediately after first login** via **Settings → Change Password**.

---

## 9. Health Checks

```bash
# API liveness
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost/health

# Database (inside container)
docker exec crms-postgres pg_isready -U crms
```

Expected: `{"status":"UP"}` from Spring Actuator.

---

## 10. Verify Installation

1. Log in as `admin@crms.local` → Dashboard loads with zero-data state
2. Navigate to **Tenders** → create a test tender
3. Navigate to **Companies** → Companies House lookup returns results (requires network + API key in `.env`)
4. Navigate to **H&S** → upload a test document (verifies MinIO connectivity)
5. Check **Settings → Audit Log** → entries appear with SHA-256 hashes

---

## 11. Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Backend exits immediately | DB not ready | `docker compose restart backend` after postgres is healthy |
| "Flyway migration failed" | Schema conflict | Check `docker compose logs backend`; if dev DB, run `docker compose down -v` to reset volumes |
| MinIO unreachable | Wrong credentials | Verify `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` in `.env` |
| JWT errors on login | `JWT_SECRET` changed | Existing tokens are invalidated — users must log in again |
| SMTP alerts not sent | Firewall blocking port 587 | Check outbound port 587 is open; try port 465 (SSL) |

For further help, see the [Configuration Reference](configuration.md) or contact Tier 2 support.
