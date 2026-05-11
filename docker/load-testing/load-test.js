import http from 'k6/http';
import { check, sleep } from 'k6';
import { RateCounter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_TOKEN = __ENV.ADMIN_TOKEN || '';

const errorRate = new RateCounter('errors');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.01'],
    errors: ['rate<0.05'],
  },
};

const headers = {
  'Content-Type': 'application/json',
  ...(ADMIN_TOKEN ? { 'Authorization': `Bearer ${ADMIN_TOKEN}` } : {}),
};

export default () => {
  const endpoints = [
    { method: 'GET', path: '/api/v1/dashboard/stats', name: 'Dashboard' },
    { method: 'GET', path: '/api/v1/tenders', name: 'List Tenders' },
    { method: 'GET', path: '/api/v1/contracts', name: 'List Contracts' },
    { method: 'GET', path: '/api/v1/operatives', name: 'List Operatives' },
    { method: 'GET', path: '/api/v1/plant', name: 'List Plant' },
    { method: 'GET', path: '/api/v1/subcontractors', name: 'List Subcontractors' },
    { method: 'GET', path: '/api/v1/reports/cvr', name: 'CVR Report' },
  ];

  const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];

  const res = http.request(endpoint.method, `${BASE_URL}${endpoint.path}`, null, { headers });

  const success = check(res, {
    [`${endpoint.name} status 200`]: (r) => r.status === 200,
    [`${endpoint.name} response time < 500ms`]: (r) => r.timings.duration < 500,
  });

  if (!success) {
    errorRate.add(1);
    console.error(`${endpoint.name} failed: ${res.status} ${res.body}`);
  }

  sleep(Math.random() * 2 + 0.5);
};

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'loadtest-results.json': JSON.stringify(data, null, 2),
  };
}

function textSummary(data, opts) {
  const { metrics } = data;
  const duration = metrics.http_req_duration;

  let summary = '\n=== CRMS Load Test Results ===\n\n';
  summary += `Duration: ${(data.state.testRunDurationMs / 1000).toFixed(1)}s\n`;
  summary += `Total Requests: ${metrics.http_reqs.values.count}\n`;
  summary += `Failed Requests: ${metrics.http_req_failed.values.passes}\n\n`;

  summary += 'Response Time:\n';
  summary += `  avg: ${duration.values.avg.toFixed(2)}ms\n`;
  summary += `  p95: ${duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `  p99: ${duration.values['p(99)'].toFixed(2)}ms\n`;
  summary += `  max: ${duration.values.max.toFixed(2)}ms\n\n`;

  summary += 'Throughput:\n';
  summary += `  avg: ${metrics.http_reqs.values.rate.toFixed(2)} req/s\n\n`;

  const failedRate = (metrics.http_req_failed.values.passes / metrics.http_reqs.values.count * 100).toFixed(2);
  summary += `Error Rate: ${failedRate}%\n`;

  const passed = failedRate < 1 && duration.values['p(95)'] < 500;
  summary += `\n${passed ? '✅ TEST PASSED' : '❌ TEST FAILED'}\n`;

  return summary;
}
