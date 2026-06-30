# Public Course List Load Test - AFTER 1000 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 후

## Summary

| Metric | Value |
|---|---:|
| Average response time | 134.44ms |
| p90 | 322.29ms |
| p95 | 426.67ms |
| p99 | 672.68ms |
| Failure rate | 0.00% |
| HTTP requests | 37,971 |
| Throughput | 372.1/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.