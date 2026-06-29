# Public Course List Load Test - AFTER 200 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 후

## Summary

| Metric | Value |
|---|---:|
| Average response time | 15.11ms |
| p90 | 18.17ms |
| p95 | 19.55ms |
| p99 | 23.14ms |
| Failure rate | 0.00% |
| HTTP requests | 8,016 |
| Throughput | 78.1/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.