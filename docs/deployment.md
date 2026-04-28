# CRMS CI G UK Deployment Guide

## Overview

CRMS CI G UK can be deployed using:
- **Docker Compose**: Single-host deployment (NAS, mini-PC, VM)
- **Kubernetes**: Scalable production deployment with Helm

## Docker Compose Deployment

### Single-Host Architecture

```
┌─────────────────────────────────────────┐
│           Host Server/NAS               │
│                                         │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │  Nginx  │  │Backend  │  │Frontend │ │
│  │  Proxy  │  │(Spring) │  │  (Vue)  │ │
│  └────┬────┘  └────┬────┘  └────┬────┘ │
│       │            │            │       │
│       └────────────┼────────────┘       │
│                    │                     │
│              ┌─────┴─────┐               │
│              │ PostgreSQL│               │
│              │    16     │               │
│              └─────┬─────┘               │
│                    │                     │
│              ┌─────┴─────┐               │
│              │   MinIO   │               │
│              │ (S3-compat)│              │
│              └───────────┘               │
└─────────────────────────────────────────┘
```

### Deployment Steps

1. **Prepare Host**
   ```bash
   # Create user
   sudo useradd -m -s /bin/bash crms
   sudo usermod -aG docker crms
   
   # Create directories
   sudo mkdir -p /opt/crms
   sudo chown crms:crms /opt/crms
   ```

2. **Install CRMS**
   ```bash
   cd /opt/crms
   curl -O https://releases.crms-ci-g-uk.co.uk/crms-ci-g-uk.tar.gz
   tar -xzf crms-ci-g-uk.tar.gz
   cd docker
   ```

3. **Configure**
   ```bash
   cp .env.example .env
   # Edit .env with production values
   ```

4. **Start Services**
   ```bash
   chmod +x scripts/setup.sh
   ./scripts/setup.sh
   ```

### Production Tuning

```yaml
# docker-compose.prod.yml additions
services:
  postgres:
    deploy:
      resources:
        limits:
          memory: 8G
    shm_size: 256m
    command:
      - postgres
      - -cshared_buffers=2GB
      - -ceffective_cache_size=6GB
      - -cmax_connections=200

  backend:
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 4G
    environment:
      JAVA_OPTS: "-Xms1g -Xmx3g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
```

## Kubernetes Deployment

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │              crms namespace                     │   │
│  │                                                  │   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐        │   │
│  │  │Frontend │  │Backend  │  │ Backend │        │   │
│  │  │  Pods   │  │  Pod 1  │  │  Pod 2  │        │   │
│  │  └────┬────┘  └────┬────┘  └────┬────┘        │   │
│  │       │            │            │               │   │
│  │       └────────────┼────────────┘               │   │
│  │                    │                              │   │
│  │  ┌─────────┐       │       ┌─────────┐         │   │
│  │  │PostgreSQL│◄──────┼──────►│ MinIO   │         │   │
│  │  │ Stateful │       │       │ Storage │         │   │
│  │  └─────────┘       │       └─────────┘         │   │
│  └─────────────────────────────────────────────────┘   │
│                         │                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │               Ingress Controller                 │   │
│  │  (TLS termination, Load balancing)              │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Prerequisites

- Kubernetes 1.27+
- Helm 3.12+
- Ingress controller (nginx-ingress)
- Cert-manager for TLS

### Installation

1. **Create Namespace and Secrets**
   ```bash
   kubectl create namespace crms
   
   kubectl create secret generic crms-secrets \
     --namespace crms \
     --from-literal=SPRING_DATASOURCE_PASSWORD='your-password' \
     --from-literal=MINIO_ACCESS_KEY='your-key' \
     --from-literal=MINIO_SECRET_KEY='your-secret' \
     --from-literal=APP_SECRET_KEY='your-32-char-secret' \
     --from-literal=JWT_SECRET='your-64-char-hex'
   ```

2. **Install with Helm**
   ```bash
   helm install crms ./helm \
     --namespace crms \
     --values ./helm/values.yaml
   ```

3. **Verify Installation**
   ```bash
   kubectl get pods -n crms
   kubectl get ingress -n crms
   ```

### Upgrading

```bash
# Update CRMS to new version
helm upgrade crms ./helm \
  --namespace crms \
  --set backend.image.tag=v1.1.0 \
  --set frontend.image.tag=v1.1.0
```

### High Availability

Enable multi-replica deployment:

```yaml
# values.yaml
backend:
  replicaCount: 3
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 10
```

## Container Registry

CRMS CI G UK images are hosted on Quay.io for China accessibility:

```bash
# Login to registry
docker login quay.io

# Pull images
docker pull quay.io/crms-ci-g-uk/backend:1.0.0
docker pull quay.io/crms-ci-g-uk/frontend:1.0.0
```

## Network Configuration

### Ports

| Port | Service | Purpose |
|------|---------|---------|
| 80 | Nginx | HTTP |
| 443 | Nginx | HTTPS |
| 5432 | PostgreSQL | Database (internal) |
| 9000 | MinIO | S3 API |
| 9001 | MinIO | Console |
| 8080 | Backend | API (internal) |

### Firewall Rules

```bash
# External access
-A INPUT -p tcp --dport 80 -j ACCEPT
-A INPUT -p tcp --dport 443 -j ACCEPT

# Admin access (restrict to admin network)
-A INPUT -p tcp --dport 9001 -s 10.0.0.0/8 -j ACCEPT
```

## Performance Tuning

### PostgreSQL

```ini
# postgresql.conf tuning
max_connections = 200
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
wal_buffers = 16MB
checkpoint_completion_target = 0.9
max_wal_size = 4GB
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 20MB
min_wal_size = 1GB
max_worker_processes = 8
```

### JVM Settings

```bash
JAVA_OPTS="-Xms1g -Xmx3g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/data/logs/ \
  -Djava.security.egd=file:/dev/./urandom"
```

## Security Hardening

### Container Security

```yaml
# docker-compose.yml additions
services:
  backend:
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
      - /run
```

### Network Isolation

```bash
# Create Docker network
docker network create --driver bridge \
  --subnet 172.20.0.0/16 \
  crms-internal
```

## Monitoring

### Health Check Script

```bash
# Add to monitoring (Prometheus, etc.)
curl -s http://localhost:8080/actuator/health | jq .status
curl -s http://localhost/ | grep -q "OK" && echo "Frontend: OK"
docker ps --format "{{.Names}}\t{{.Status}}" | grep crms
```

## Troubleshooting

### Pod CrashLoopBackOff

```bash
kubectl describe pod backend-xxx -n crms
kubectl logs backend-xxx -n crms --previous
```

### Database Connection Issues

```bash
# Check credentials
kubectl get secret crms-secrets -n crms -o yaml

# Test connection
kubectl run -it --rm debug \
  --image=postgres:16-alpine \
  --restart=Never \
  -- psql -h postgres-service -U crms -d crms
```

### Ingress Issues

```bash
# Check ingress controller logs
kubectl logs -n ingress-nginx deploy/ingress-nginx-controller

# Check certificate
kubectl describe certificate -n crms
```
