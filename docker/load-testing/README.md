# CRMS Load Testing

This directory contains k6 load testing scripts for validating CRMS performance under realistic load.

## Prerequisites

- [k6](https://k6.io/docs/getting-started/installation/) installed
- Backend running at `http://localhost:8080`
- Database seeded with test data

## Quick Start

### 1. Start the Backend

```bash
cd /workspace/docker
docker compose up -d backend db redis
```

### 2. Run Load Tests

```bash
# Navigate to this directory
cd docker/load-testing

# Run with default settings (5 VUs, 30s ramp-up to 100 VUs)
k6 run load-test.js

# Run with custom credentials
ADMIN_USERNAME=admin ADMIN_PASSWORD=yourpass k6 run load-test.js

# Point to different backend URL
BASE_URL=http://staging.crms.local k6 run load-test.js
```

## Test Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `BASE_URL` | `http://localhost:8080` | Backend base URL |
| `ADMIN_USERNAME` | `admin` | Login username |
| `ADMIN_PASSWORD` | `admin123` | Login password |

### Load Test Stages

The default test runs through these stages:

1. **Ramp-up**: 30s → 10 VUs
2. **Sustained**: 1m → 50 VUs
3. **Peak**: 2m → 100 VUs
4. **Cool-down**: 1m → 50 VUs
5. **Ramp-down**: 30s → 0 VUs

### Thresholds

| Metric | Threshold | Description |
|--------|-----------|-------------|
| `http_req_duration` | p(95) < 500ms | 95% of requests under 500ms |
| `http_req_duration` | p(99) < 1000ms | 99% of requests under 1s |
| `http_req_failed` | rate < 0.01 | Less than 1% failure rate |

## Expected Results

A successful test should show:

```
=== CRMS Load Test Results ===

Duration: 300.0s
Total Requests: ~10,000
Failed Requests: < 100

Response Time:
  avg: < 100ms
  p95: < 500ms
  p99: < 1000ms
  max: < 2000ms

Throughput:
  avg: > 30 req/s

Error Rate: < 1%

✅ TEST PASSED
```

## Other Test Scripts

### `stress-test.js`

Aggressive stress test with higher VUs:

```bash
k6 run stress-test.js
```

### `smoke-test.js`

Quick validation test (10 VUs, 30s):

```bash
k6 run smoke-test.js
```

## Interpreting Results

### High Error Rate

- Check backend logs for exceptions
- Verify database connections (HikariCP pool size)
- Check for rate limiting (429 responses)
- Review API endpoint performance

### High Latency

- Database query optimization (add indexes)
- Enable caching (Redis)
- Check connection pool settings
- Review N+1 query patterns

### Low Throughput

- Database bottleneck (check slow query log)
- Insufficient connection pool size
- Network latency
- CPU/thread exhaustion

## CI/CD Integration

Add to your CI pipeline:

```yaml
# .github/workflows/load-test.yml
- name: Run Load Tests
  run: |
    cd docker/load-testing
    k6 run load-test.js \
      --out json=loadtest-results.json \
      --summary-export=loadtest-summary.json
  env:
    ADMIN_PASSWORD: ${{ secrets.LOAD_TEST_PASSWORD }}
```

## Cleanup

Results are saved to `loadtest-results.json` in the current directory.

```bash
rm -f loadtest-results.json loadtest-summary.json
```
