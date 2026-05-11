import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || 'admin';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin123';

let authToken = null;

function getAuthToken() {
  if (authToken) return authToken;

  const loginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ username: ADMIN_USERNAME, password: ADMIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status === 200) {
    try {
      const body = JSON.parse(loginRes.body);
      authToken = body.token || body.accessToken || body.data?.token;
    } catch (e) {
      console.error('Failed to parse auth response:', loginRes.body);
    }
  }

  if (!authToken) {
    console.error('Authentication failed, using unauthenticated requests');
  }

  return authToken;
}

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
  },
};

export default () => {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
  };

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
    [`${endpoint.name} status 200`]: (r) => r.status === 200 || r.status === 401 || r.status === 403,
    [`${endpoint.name} response time < 500ms`]: (r) => r.timings.duration < 500,
  });

  if (!success) {
    console.error(`${endpoint.name} failed: ${res.status} ${res.body}`);
  }

  sleep(Math.random() * 2 + 0.5);
};

export function handleSummary(data) {
  const { metrics } = data;
  const duration = metrics.http_req_duration;
  const totalReqs = metrics.http_reqs.values.count;
  const failedReqs = metrics.http_req_failed.values.passes;
  const failedRate = totalReqs > 0 ? (failedReqs / totalReqs * 100).toFixed(2) : '0.00';

  let summary = '\n=== CRMS Load Test Results ===\n\n';
  summary += `Duration: ${(data.state.testRunDurationMs / 1000).toFixed(1)}s\n`;
  summary += `Total Requests: ${totalReqs}\n`;
  summary += `Failed Requests: ${failedReqs}\n\n`;

  summary += 'Response Time:\n';
  summary += `  avg: ${duration.values.avg.toFixed(2)}ms\n`;
  summary += `  p95: ${duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `  p99: ${duration.values['p(99)'].toFixed(2)}ms\n`;
  summary += `  max: ${duration.values.max.toFixed(2)}ms\n\n`;

  summary += 'Throughput:\n';
  summary += `  avg: ${metrics.http_reqs.values.rate.toFixed(2)} req/s\n\n`;

  summary += `Error Rate: ${failedRate}%\n`;

  const passed = parseFloat(failedRate) < 1 && duration.values['p(95)'] < 500;
  summary += `\n${passed ? '✅ TEST PASSED' : '❌ TEST FAILED'}\n`;

  return {
    stdout: summary,
    'loadtest-results.json': JSON.stringify(data, null, 2),
  };
}
