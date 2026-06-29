# Public Course List Load Test - BEFORE 200 VUs

## Test Target

- API: GET /api/v1/courses/countries/{countryId}
- COUNTRY_ID: 10
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis 캐싱 적용 전

## Summary

| Metric | Value |
|---|---:|
| Average response time | 27.54ms |
| p90 | 31.49ms |
| p95 | 33.02ms |
| p99 | 39.38ms |
| Failure rate | 0.00% |
| HTTP requests | 8,000 |
| Throughput | 78.3/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the public course list Redis caching test.