import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '30s', target: 50 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    // At least 95% of all checks must pass
    checks: ['rate > 0.95'],

    // Optional: 95th percentile of response time for *successful* requests < 500ms
    'http_req_duration{expected_response:true}': ['p(95) < 500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_KEY = __ENV.API_KEY || 'dev-key-123'; // must match shortener.api-key locally


export function setup() {
  const payload = JSON.stringify({
    targetUrl: 'https://google.com',
    customAlias: 'mario-long',
  });

  const res = http.post(`${BASE_URL}/links`, payload, {
    headers: { 'Content-Type': 'application/json' },
    'X-API-KEY': API_KEY, // 🔐 added API key
  });

  check(res, {
    'created mario-long': (r) =>
      r.status === 200 || r.status === 201 || r.status === 409,
  });

  return {};
}

export default function () {
  const redirectRes = http.get(`${BASE_URL}/r/mario-long`, { redirects: 0 });
  const detailsRes = http.get(`${BASE_URL}/links/mario-long`);

  check(redirectRes, {
    'redirect status is 302': (r) => r.status === 302,
  });

  check(detailsRes, {
    'details status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
