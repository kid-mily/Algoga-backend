# Learning Progress Write-Behind Load Test - AFTER 500 VUs

## Test Target

- API: POST /api/v1/courses/{courseId}/chapters/{chapterId}/progress
- COURSE_ID: 76
- CHAPTER_ID: 66
- LOGIN_USERNAME: 	est1
- Max VUs: $(System.Collections.Hashtable.vu)
- Scenario: ramping-vus, 20s ramp-up / 1m steady / 20s ramp-down
- Phase: Redis Write-Behind 적용 후

## Summary

| Metric | Value |
|---|---:|
| Average response time | 17.35ms |
| p90 | 20.28ms |
| p95 | 21.54ms |
| p99 | 27.74ms |
| Failure rate | 0.00% |
| HTTP requests | 20,119 |
| Throughput | 195.95/s |

## Note

This file is a normalized summary for PR and presentation evidence. Values were recorded from the K6 terminal output captured during the learning progress Redis Write-Behind test.