# CRMS Load Testing

Performance testing scripts using k6 for load testing the CRMS backend.

## Prerequisites

```bash
# Install k6
# macOS
brew install k6

# Linux
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

## Run Load Test

### 1. Start the application
```bash
cd docker
docker compose up -d
```

### 2. Get an admin token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@crms.com","password":"Admin123!"}' \
  | jq -r '.data.token'
```

### 3. Run the load test
```bash
# Without authentication
k6 run load-test.js

# With authentication
ADMIN_TOKEN=your_token_here k6 run load-test.js

# Use staging URL
BASE_URL=https://staging.your-domain.com k6 run load-test.js
```

## Test Scenarios

The load test simulates realistic usage:
- **Ramp up**: 10 → 50 → 100 → 50 → 0 users
- **Duration**: ~5 minutes
- **Endpoints tested**: Dashboard, Tenders, Contracts, Operatives, Plant, Subcontractors, Reports

## Success Criteria

| Metric | Threshold |
|--------|-----------|
| Response Time p95 | < 500ms |
| Response Time p99 | < 1000ms |
| Error Rate | < 1% |

## Output

- Console summary printed to stdout
- JSON results saved to `loadtest-results.json`

## Continuous Load Testing

For sustained load testing:
```bash
# 10 minutes at 50 concurrent users
k6 run --duration 10m --vus 50 load-test.js
```
