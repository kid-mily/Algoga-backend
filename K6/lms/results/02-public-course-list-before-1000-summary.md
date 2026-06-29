# Public Course List Load Test - BEFORE 1000 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 전

## Summary

| Metric | Value |
|---|---:|
| Average response time | 1.22s |
| p90 | 1.83s |
| p95 | 1.98s |
| p99 | 2.38s |
| Failure rate | 0.00% |
| HTTP requests | 25,235 |
| Throughput | 247.2/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.