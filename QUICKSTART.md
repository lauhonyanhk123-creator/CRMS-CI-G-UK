# CRMS CI G UK - Quick Start Guide

## TL;DR

```bash
git clone https://github.com/lauhonyanhk123-creator/CRMS-CI-G-UK.git
cd CRMS-CI-G-UK/docker
cp .env.example .env
docker compose -f docker-compose.dev.yml up -d
# Wait 2-3 minutes
# Open http://localhost:5173
# Login: admin@crms.local / Admin123!
```

**Done!** 🎉

---

## For Developers

### Frontend Development

```bash
# Start frontend with hot reload
cd frontend
pnpm install
pnpm dev
# Open http://localhost:5173
```

### Backend Development

```bash
# Start backend
cd backend
./mvnw spring-boot:run
# API at http://localhost:8080
# Swagger at http://localhost:8080/swagger-ui.html
```

### Run Tests

```bash
# Frontend tests
cd frontend
pnpm test          # Run all tests
pnpm test:watch    # Watch mode
pnpm typecheck     # TypeScript checking

# Backend tests
cd backend
./mvnw test
./mvnw verify
```

---

## All Services

| Service | Command | URL |
|---------|---------|-----|
| Frontend | `docker compose up frontend` | http://localhost:5173 |
| Backend | `docker compose up backend` | http://localhost:8080 |
| PostgreSQL | `docker compose up postgres` | localhost:5432 |
| MinIO | `docker compose up minio` | http://localhost:9001 |
| Redis | `docker compose up redis` | localhost:6379 |
| All | `docker compose up -d` | - |

---

## First Login

**Username:** `admin@crms.local`  
**Password:** `Admin123!`

⚠️ **Change password immediately after first login!**

---

## Common Commands

```bash
# Start everything
docker compose up -d

# Stop everything
docker compose down

# View logs
docker compose logs -f

# Check status
docker compose ps

# Restart a service
docker compose restart backend

# Update
git pull
docker compose build
docker compose up -d
```

---

## Troubleshooting

**Nothing works?**
```bash
# Check logs
docker compose logs

# Restart everything
docker compose down
docker compose up -d
```

**Port already in use?**
```bash
# Check what's using the port
lsof -i :5173

# Kill it or change port in docker-compose.yml
```

**Need help?**
- Full guide: [SETUP_GUIDE.md](SETUP_GUIDE.md)
- Tests: All 91 tests passing ✅
- TypeScript: No errors ✅
- Status: Ready for deployment 🚀

---

## What's Next?

1. ✅ System is running!
2. Login and explore all 12 modules
3. Test the login flow
4. Try creating a tender/contract
5. Test the offline PWA capabilities

**Happy testing!**
