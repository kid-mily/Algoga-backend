# Public Course List Load Test - AFTER 500 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 후

## Summary

| Metric | Value |
|---|---:|
| Average response time | 16.99ms |
| p90 | 19.33ms |
| p95 | 20.51ms |
| p99 | 25.65ms |
| Failure rate | 0.00% |
| HTTP requests | 20,075 |
| Throughput | 197.2/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.