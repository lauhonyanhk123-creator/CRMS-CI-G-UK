# CRMS Frontend

React frontend for the Contract Management System (CRMS CI G UK).

## Prerequisites

- Node.js 18+
- npm 9+

## Setup

```bash
cd frontend
npm install
```

## Development

```bash
npm run dev
```

Starts at `http://localhost:5173`. API calls are proxied to `http://localhost:8080`.

## Build

```bash
npm run build
```

Output goes to `dist/`.

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

## Auth

Login sets a JWT token in localStorage and attaches it as a Bearer header on every request. Logout clears the token.

## API Proxy

Vite proxies `/api` to the backend. No CORS configuration needed on the backend.
