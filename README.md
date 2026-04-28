# CRMS CI G UK

**Construction Resource Management System for the UK Groundworks Industry**

CRMS CI G UK is a **single-tenant, self-hosted, perpetual-licence operations platform** purpose-built for UK groundwork contractors with turnover between £5m and £50m and a head-count of 20–200. It runs as a small set of services on a Synology/QNAP NAS, a Windows Server box, or a Linux mini-PC inside the contractor's own office, and works fully on the LAN with no cloud dependency for core operation.

> **Product version 1.0** — built to the Technical Specification CRMS CI G UK TSD v1.0 (27 April 2026)

---

## At a Glance

| Dimension | Detail |
|-----------|--------|
| **Deployment** | Single-tenant, self-hosted on-premise |
| **Hardware** | Synology DS923+/DS1522+, Linux mini-PC (Ryzen 7), or Windows Server 2022 |
| **Stack** | Java 21 / Spring Boot 3 · Vue 3 / Element Plus / ECharts · PostgreSQL 16 · MinIO · Nginx |
| **Licence** | Perpetual, per-installation, no per-user or per-volume charges |
| **Tiers** | Yard (£18k, ≤15 users) · Site (£36k, ≤40 users) · Group (£72k, ≤100 users) |
| **Starting price vs incumbent** | 4–7 year payback vs £80k–£150k/yr six-vendor estate |

---

## Module Overview

CRMS CI G UK is a modular monolith (one deployable JAR, schema-namespace isolation). All 12 modules ship in v1.0:

| # | Module | Key Artefacts |
|---|--------|---------------|
| 1 | **Sales & Tender Pipeline** | Enquiry tracking, Kanban (Lead→Awarded), BoQ, CDE document register, win/loss analytics |
| 2 | **Estimating & BoQ Library** | 276+ NRM2/CESMM4-coded items (muckaway, drainage, concrete, slabs, sub-base, kerbs, road, ducting), CITE-XML import, IFC take-off |
| 3 | **Contract & Commercial Control** | Contract creation from tender, JCT/NEC4 capture, applications-for-payment cycle, Construction Act payment/pay-less/default notices, SHA-256 audit, adjudication evidence pack, CVR engine, WIP journal |
| 4 | **Subcontractors, CIS & Supply Chain** | CIS300 return generation and HMRC API submission, Payment & Deduction Statements, Companies House auto-refresh, Confirmation of Payee, subbie gate (CIS + CSCS + RAMS + induction + plant ticket check) |
| 5 | **Operatives, Payroll Bridge & Competence** | CSCS/CPCS/NPORS cards, qualifications (NVQ, SMSTS, SSSTS, EUSR, IPAF, PASMA), 60/30/14/7-day expiry alerts, structured FPS-ready timesheet export |
| 6 | **Plant Register & LOLER/PUWER Calendar** | Owned/hired/cross-hired plant, LOLER (6/12-monthly), PUWER, daily pre-use checks, plant Gantt, CPCS operator allocation check, hire records under CPA conditions |
| 7 | **Materials, Procurement & Delivery** | PR → PO → GRN workflow, concrete pour workflow (delivery tickets, slump, water-added, cube samples, 7/28-day strength cusum), muckaway tickets with landfill tax (£126.15/t standard, £4.05/t inert, 2025/26) |
| 8 | **Health, Safety & CDM 2015** | F10 notification, CPP (Construction Phase Plan), 30 RAMS templates, digital RAMS sign-on on PWA, permits to dig (HSG47 third edition), permits to load/strike (BS 5975-1:2024), mandatory occurrence reporting |
| 9 | **Quality, ITPs & Inspections** | ITP library (CBR, plate-bearing, TR34 flatness, drainage air/water test, CCTV MSCC5), external inspection tracking (NHBC Chapters 4.1–5.3, LABC, Local Authority Highways, Water Authority S104), defect close-out |
| 10 | **Section 38 / 278 / 104 Adoption Workflow** | Multi-year case files, bonds (10–25% works value), 4-stage Road Safety Audits, CCTV/air-water testing, Provisional Certificate, 12-month maintenance period, commuted sums, **automatic bond-release alerting at 90/60/30/14/7/0 days** |
| 11 | **Site PWA (Progressive Web App)** | Offline-first, IndexedDB queue, GPS-tagged photos, site sign-on with face capture, toolbox talks, dayworks sheets, plant pre-use checks, muckaway/concrete ticket capture, defect raise, near-miss/incident report with RIDDOR check |
| 12 | **Reporting Suite** | CVR pack, cash-flow forecast, retention schedule, CIS300 pack, CITB levy estimator, plant utilisation, H&S statistics (AFR), subcontractor performance, adoption status, payment practices (PPPR 2017) |

---

## Integration Points

| Service | Protocol | Purpose |
|---------|----------|---------|
| HMRC CIS Deductions API | REST / OAuth 2.0 | CIS300 submission, subcontractor verification |
| HMRC MTD (ITSA) API | REST / OAuth 2.0 | VAT returns, CIS MTD compliance |
| Companies House API | REST / Basic Auth | Company profiles, charges, PSC, filing history |
| CSCS Smart Check API | REST | Card validation across 37+ schemes (replaced Go Smart 31 Mar 2024) |
| LinesearchbeforeUdig | REST | Statutory undertaker plans for permit-to-dig |
| Open Banking AISP/PISP | PSD2 | Bank statement reconciliation, Confirmation of Payee |
| HSE F10 | Web form | CDM notification submission |
| Bacs Direct Credit | Standard 18 file | Subcontractor payments |

---

## Project Structure

```
crms-ci-g-uk/
├── backend/                    # Spring Boot 3 / Java 21 REST API
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/crms/
│       │   ├── config/         # Security, JWT, MinIO, CORS, OpenAPI, Async, Cache, Quartz, DataInitializer
│       │   ├── domain/         # JPA entities organised by module (contract/, plant/, operative/, etc.)
│       │   │   ├── adoption/
│       │   │   ├── common/
│       │   │   ├── company/
│       │   │   ├── contract/
│       │   │   ├── healthsafety/
│       │   │   ├── material/
│       │   │   ├── operative/
│       │   │   ├── plant/
│       │   │   ├── subcontractor/
│       │   │   ├── tender/
│       │   │   └── user/
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   └── response/
│       │   ├── service/        # 18 service interfaces + implementations
│       │   ├── web/            # 18 REST controllers
│       │   └── security/      # JWT, Casbin RBAC, AuditLogAspect
│       └── resources/
│           ├── application.yml
│           └── db/migration/  # V1 (schema), V2 (BoQ library), V3 (RAMS templates)
├── frontend/                   # Vue 3 / Vite / Element Plus / ECharts SPA + PWA
│   ├── src/
│   │   ├── views/             # 20 view components by domain
│   │   ├── components/        # Reusable: DataTable, FileUpload, StatsCard, StatusBadge
│   │   ├── composables/       # useApi, useAuth, useOfflineSync, usePWA, usePermission
│   │   ├── services/          # Axios API client
│   │   ├── stores/            # Pinia stores (auth, app)
│   │   └── router/            # Vue Router with auth guards
│   ├── public/                # PWA manifest, icons
│   ├── package.json
│   └── vite.config.ts
├── docker/                     # Docker, Docker Compose, Helm, Kubernetes manifests
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   ├── docker-compose.yml     # Production
│   ├── docker-compose.dev.yml # Development
│   ├── nginx/
│   ├── nginx.conf
│   ├── helm/                  # Helm 3 chart
│   ├── kubernetes/            # K8s manifests
│   └── scripts/               # setup.sh, backup.sh, restore.sh, healthcheck.sh
├── docs/                      # Installation, Configuration, Backup, Deployment, API reference
├── kubernetes/               # K8s cluster manifests
└── README.md
```

---

## Getting Started

### Prerequisites

- Docker & Docker Compose **or** Kubernetes 1.28+ with Helm 3
- PostgreSQL 16 (included in Docker Compose)
- 16 GB RAM minimum (32 GB recommended for Site tier)
- 500 GB storage minimum

### 1. Clone and Configure

```bash
git clone https://github.com/crms-ci-g-uk/crms-ci-g-uk.git
cd crms-ci-g-uk/docker
cp .env.example .env
# Edit .env: set DB_PASSWORD, JWT_SECRET, MinIO credentials
```

### 2. Start (Docker Compose — Production)

```bash
docker-compose up -d
```

### 3. Start (Docker Compose — Development)

```bash
docker-compose -f docker-compose.dev.yml up -d
# Frontend dev server on http://localhost:5173
# Backend API on http://localhost:8080/api/v1
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### 4. Helm on Kubernetes

```bash
cd docker/helm
helm install crms . --namespace crms --create-namespace --values values.yaml
```

### 5. First Login

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

> Change all passwords on first login.

---

## Documentation

| Document | Description |
|----------|-------------|
| [Installation Guide](docs/installation.md) | Hardware sizing, OS prep, Docker/K8s install, first-run |
| [Configuration Reference](docs/configuration.md) | Every `application.yml` / env var, with defaults |
| [Backup & Recovery](docs/backup.md) | 3-2-1 strategy, pg_basebackup, MinIO replication, restore drill |
| [API Documentation](docs/api.md) | Full REST API reference (~280 endpoints) |
| [Deployment Guide](docs/deployment.md) | Synology, Windows Server, Linux mini-PC, cloud options |

---

## Security

- **Authentication**: Local credentials + Argon2id password hashing, optional TOTP 2FA
- **Sessions**: Short-lived JWT (15 min) with revocable opaque refresh tokens
- **Authorisation**: RBAC + attribute scoping via jCasbin (Casbin policies bound to contract owner, site assignment)
- **Audit**: Immutable audit log for every state-changing action, SHA-256 hash per entry, daily Merkle root
- **UK GDPR**: Lawful-basis register, SAR workflow, configurable retention schedules, DPIA for biometric data
- **Encryption**: All passwords hashed (never stored plaintext), backups AES-256-GCM with customer-held key

---

## Backup & DR

| RTO | RPO |
|-----|-----|
| < 4 hours on commodity hardware | 1 hour (WAL-based) |

Default strategy: nightly full + hourly WAL on-host, replicated to second NAS over LAN, optional encrypted offsite copy to Backblaze B2 / AWS S3 / Wasabi.

---

## Hardware Sizing

| Tier | Users | Min RAM | Min Storage | Recommended |
|------|-------|---------|-------------|-------------|
| Yard | ≤15 | 16 GB ECC | 4 TB | Synology DS923+ / Beelink Ryzen 7 |
| Site | ≤40 | 32 GB | 8 TB | Synology DS1522+ / Dell PowerEdge T350 |
| Group | ≤100 | 64 GB | 16 TB | HPE ProLiant ML30 / Dell PowerEdge T350 |

Typical data growth: 2–4 GB structured + 200–500 GB attachments per year.

---

## Support

- **Tier 1**: In-product help, video library, PDF guides (offline-available)
- **Tier 2**: UK business-hours phone/email — 4-business-hour SLA (included in maintenance)
- **Tier 3**: Engineering escalation to Chinese team via Tier 2

---

## Licence

Proprietary — Perpetual Licence. Based on SCL model contract with UK GDPR controller/processor clauses, source-code escrow (NCC Group), data export rights, and 90-day transition assistance.

---

## Why This Product / Why Now

**Timing is sharp.** AMP8 commits £104bn to water infrastructure 2025–2030. The Building Safety Regulator's Gateway 2 fast-track cleared in February 2026. CLC December 2025 guidance enables staged foundations/groundworks approvals. Future Homes Standard published 24 March 2026 (in force 24 March 2027) mandates ground source heat pumps and tighter foundation tolerances. **MTD for ITSA is mandatory for CIS subcontractors over £50k from 6 April 2026.** The Procurement Act 2023 Central Digital Platform has been live since 24 February 2025.

**The threat landscape forces self-hosted.** Construction was the third most-targeted ransomware sector globally in 2024. The Dodd Group breach (October 2025) leaked 4 TB including data on RAF and Royal Navy bases. SaaS prices are rising 11–12% annually for the third year running.

**No incumbent fills all eight gaps.** CRMS CI G UK is the only product that delivers: genuinely self-hosted single-tenant deployment, unified subbie gate (HMRC CIS + CSCS Smart Check + RAMS + induction), a pre-loaded UK groundworks BoQ library (276 items), a plant register with live LOLER calendar and operator allocation, Section 38/278/104 bond-release tracking, CDM 2015 document control, a true offline-first PWA, and a groundworks-specific CVR engine — all in one product.
