# Public Course List Redis Caching - Before/After Comparison

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down

## Result Table

| VU | Before Avg | After Avg | Before p95 | After p95 | Before TPS | After TPS | Failure Rate |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 200 | 27.54ms | 15.11ms | 33.02ms | 19.55ms | 78.3/s | 78.1/s | 0% -> 0% |
| 500 | 190.33ms | 16.99ms | 928.37ms | 20.51ms | 180.0/s | 197.2/s | 0% -> 0% |
| 800 | 575.56ms | 28.31ms | 1.07s | 56.99ms | 246.5/s | 312.1/s | 0% -> 0% |
| 1000 | 1.22s | 134.44ms | 1.98s | 426.67ms | 247.2/s | 372.1/s | 0% -> 0% |

## Interpretation

Redis caching reduced response latency significantly under higher concurrency. At 1000 VUs, p95 improved from 1.98s to 426.67ms, and throughput increased from about 247 req/s to 372 req/s while keeping the failure rate at 0%.