# Public Course List Load Test - BEFORE 500 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 전

## Summary

| Metric | Value |
|---|---:|
| Average response time | 190.33ms |
| p90 | 691.92ms |
| p95 | 928.37ms |
| p99 | 1.41s |
| Failure rate | 0.00% |
| HTTP requests | 18,492 |
| Throughput | 180.0/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.