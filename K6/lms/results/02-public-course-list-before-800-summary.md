# Public Course List Load Test - BEFORE 800 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 전

## Summary

| Metric | Value |
|---|---:|
| Average response time | 575.56ms |
| p90 | 948.62ms |
| p95 | 1.07s |
| p99 | 1.38s |
| Failure rate | 0.00% |
| HTTP requests | 25,192 |
| Throughput | 246.5/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.