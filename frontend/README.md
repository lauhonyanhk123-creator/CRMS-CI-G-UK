# CRMS Frontend

Vue 3 / Element Plus frontend for CRMS CI G UK.

## Prerequisites

- Node.js 20+
- pnpm 9+

## Setup

```bash
cd frontend
pnpm install
```

## Development

```bash
pnpm dev
```

Starts at `http://localhost:5173`. API calls are proxied to `http://localhost:8080`.

## Build

```bash
pnpm build
```

Output goes to `dist/`.

## Type-checking

```bash
pnpm typecheck
```

## Lint & Format

```bash
pnpm lint      # ESLint (auto-fix)
pnpm format    # Prettier
```

## Pages

| Route | Description |
|-------|-------------|
| `/login` | Login |
| `/` | Dashboard |
| `/contracts` | Contracts CRUD |
| `/sites` | Sites CRUD |
| `/companies` | Companies CRUD |
| `/tenders` | Tenders CRUD |
| `/health-safety` | H&S dashboard, F10, incidents |
| `/operatives` | Operatives & payroll |
| `/plant` | Plant register & LOLER calendar |
| `/subcontractors` | Subcontractors & CIS |
| `/materials` | Materials & procurement |
| `/payments` | Applications, variations, dayworks, retention |
| `/reports` | CVR pack, cash-flow, CIS300, reporting suite |

## Auth

Login stores a JWT token in `localStorage` and attaches it as a `Bearer` header on every API request. Logout clears the token.

## API Proxy

Vite proxies `/api` to the backend during development — no CORS configuration needed. In production, Nginx handles the reverse proxy.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Vue 3 (Composition API) |
| UI Library | Element Plus |
| Charts | ECharts |
| State | Pinia |
| Routing | Vue Router 4 |
| HTTP | Axios |
| Build | Vite 6 |
| PWA | vite-plugin-pwa + Workbox |
| Styling | Tailwind CSS + Element Plus tokens |
| Types | TypeScript 5 + vue-tsc |
| Lint | ESLint + eslint-plugin-vue |
| Format | Prettier |
