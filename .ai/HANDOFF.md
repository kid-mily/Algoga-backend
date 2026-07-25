# HANDOFF

## Current Update 2026-07-25

- Module 5 presentation seed SQL was added:
  1. `db/seed/module5-demo-data.sql`
  2. `db/seed/module5-demo-data-rollback.sql`
- This is SQL-only; no Java code or API contracts were changed.
- The seed creates realistic manager-demo data across users, referral/friend, coupon/mileage, payment/enrollment/progress/quiz/completion/review/QnA/diagnosis, booking, and refund flows.
- Existing PKs are resolved at runtime via SQL lookups/temporary tables; the SQL does not assume fixed lecture/chapter/quiz/accommodation/package/manager IDs.
- All seeded users share the login password `password123`; the stored hash was generated and verified with Spring Security BCrypt.
- Before using on a shared DB, run the seed in a staging copy first, then run the rollback to confirm only the `M5` keyed rows are removed.

## Current Update 2026-07-24

- Active branch: `refactor/lms-dead-code-and-performance-dashboard` (created from latest `develop` after fast-forward pull).
- Current scope: LMS Grafana performance dashboard preparation plus confirmed dead-code cleanup. No API URL/request/response/JSON contract changes.
- Pre-existing local edits were saved in `stash@{0}` before switching branches. The dashboard file was restored into this branch; the prior `quiz/application/service/QuizService.java` local edit remains preserved in the stash and is intentionally not in this branch's working tree.
- Completed:
  1. Updated/restored `monitoring/grafana/dashboards/algoga-lms.json` for presentation-ready before/after panels: public course list N+1, classroom progress lookup, progress Redis flow, HikariCP, CPU/JVM, logs.
  2. Removed unused `CourseQnaComment.delete()` from `qna/domain/model/CourseQnaComment.java`.
  3. Verified dashboard JSON parsing and duplicate panel IDs.
  4. Ran `./gradlew compileJava` successfully.
- Next recommended step: commit this small dashboard/dead-code cleanup slice, then measure Grafana/K6 baseline before applying optimization code changes.

## Current Goal

- Fix a set of correctness bugs (validation gaps, concurrency races, a duplicate-submission policy gap) found via a code-review pass across the LMS-adjacent domains, without changing any API URL/request/response/JSON contract.
- Branch: `fix/lms-domain-concurrency-and-validation-bugs` (off `develop`). **Not committed yet.**

## Completed Recently

- All 6 items below are implemented, compiled, and unit-tested (see `.ai/WORKLOG.md` 2026-07-21 entry for full file-level detail). Full test suite: 159 tests, 9 pre-existing/unrelated failures (confirmed via `git stash -u` before starting).

## Current Slice Details

1. **Diagnosis** — `DiagnosisInputValidator.validateAnswerCoverage` added; `DiagnosisService.submitResult` now rejects a submission that doesn't cover every active question for the country (previously a partial submission could still be graded/scored).
2. **Quiz** — one-attempt-only enforced. New `QuizErrorCode.QUIZ_ALREADY_SUBMITTED` (409, `LMS_044`). `QuizService.submitQuiz` checks `existsByUserIdAndCourseId` before grading. `QuizSubmissionRepositoryAdapter.save()` no longer has an "update existing submission" path — always a fresh `saveAndFlush()` insert in the *same* ambient transaction (intentionally no `REQUIRES_NEW`, so a later answers-save failure rolls the submission back too); `DataIntegrityViolationException` (concurrent-submission race) maps to the same `QUIZ_ALREADY_SUBMITTED`.
3. **Certificate** — `CourseCompletionRegistrar` retries certificate-code generation up to 5x on unique-constraint collision via a new `CourseCompletionInsertTransactionExecutor` (`REQUIRES_NEW` per attempt, so a failed attempt doesn't poison the persistence context for the next retry); a `freshlyCreated` flag prevents double-publishing the completion-reward event when a request loses a concurrent-completion race. `CourseCompletionRepositoryAdapter.save()` uses `saveAndFlush()`.
4. **Learning progress** — `LearningProgressRedisAdapter.write()` uses an atomic Lua compare-and-set (skips writing if the cached `watchedSeconds` is already higher) instead of a blind `SET`, closing a lost-update race under concurrent progress-update requests.
5. **QnA** — `CourseQnaService.validateParentComment` now also rejects a soft-deleted parent comment (previously you could reply to a hidden comment, producing an orphaned reply).
6. **Course review** — a review hidden by an admin now counts as "already reviewed": `CourseReviewService.createReview`'s duplicate check changed from `existsByUserIdAndCourseIdAndDeletedFalse` to a deleted-status-agnostic `findByUserIdAndCourseId(...).isPresent()`, so resubmission is cleanly rejected (`REVIEW_ALREADY_EXISTS`, 409) instead of crashing on the DB's `(user_id, lecture_id)` unique constraint. This was a deliberate product decision (confirmed): hidden review = no rewrite at all, not an auto-restore/overwrite.

## Known Deferred Issue (do not "fix" casually — read this first)

`CourseCompletionRegistrar`'s `REQUIRES_NEW`-based retry (item 3) commits the `CourseCompletion` row independently of the caller's (`QuizService.submitQuiz` / `CourseService.completeCourse`) ambient transaction. If that ambient transaction fails at final commit *after* `register()` returns (rare — connection loss, commit-time lock/deadlock, etc.), the completion row stays committed while the quiz submission/answers roll back, and the `AFTER_COMMIT`-phase reward/notification events never fire — permanently, since a later retry just finds the existing completion and skips re-publishing.

This was found and discussed but **intentionally left unfixed** this session — it's a narrow window and the fix isn't a simple one-liner. Do not fix it by just deleting `REQUIRES_NEW` (that's what quiz's fix did, but quiz has a *no-retry* policy; certificate generation genuinely needs isolated retry attempts, so naively removing `REQUIRES_NEW` here would reintroduce the certificate-code-collision crash it was added to solve). Candidates discussed, none chosen yet:
- `Propagation.NESTED` (JDBC savepoint) instead of `REQUIRES_NEW`, so a failed attempt rolls back to a savepoint within the *same* physical transaction instead of committing independently — needs verifying the transaction manager/driver actually honors savepoints as expected.
- Generate certificate codes from a near-collision-free scheme (e.g. more entropy / sequence-based) so the retry path is effectively never exercised in practice, and accept a single ambient transaction.
- Simplify: put completion insert in the ambient transaction directly and accept that a genuine collision is rare enough to just fail loudly (with a clean error) rather than retry.

## Verification Run

- `./gradlew compileJava` / `compileTestJava` passed after each change.
- `./gradlew test`: 159 tests, 9 failing — all confirmed pre-existing/unrelated (DB/Redis-dependent Spring context-loading tests with no local infra in this environment, plus one already-broken `CourseRewardServiceTest`).
- Item 4's Lua script is only verified at the "adapter calls it with the right args" level (Mockito) — no real Redis available here to verify the script's actual CAS behavior at runtime. Recommend a manual/integration check before merging.

## Next Suggested Step

1. Decide how to resolve the deferred `CourseCompletionRegistrar` issue above, or explicitly accept the risk and move on.
2. Commit (nothing committed yet on this branch) and open a PR.
3. Manually verify the Redis Lua CAS script against a real Redis instance if possible before merging.
4. A larger backlog of optimization/cleanup items (N+1 queries in several places, missing caching on a few read-heavy endpoints, a `CurrentUserIdResolver` reflection helper duplicated across ~10 packages instead of living in `global`, a couple of small dead-code items) was identified during this session's review passes but is out of scope here — surfaced to the user, not yet scheduled. Ask before picking any of it up.

## Current Update 2026-07-26 Admin LMS Performance Optimization

- Completed admin LMS performance optimization and measurement for two presentation-heavy APIs: admin course list and coupon statistics.
- API contracts were preserved: no API URL/request/response/json changes.
- Main result:
  - GET /api/v1/admin/courses: p95 1.64s -> 9.47ms, p99 1.89s -> 18.25ms, throughput 266.29 -> 389.27 req/s, error 0%.
  - GET /api/v1/admin/coupon-statistics: p95 3.34s -> 12.09ms, p99 3.63s -> 18.21ms, throughput 190.09 -> 390.12 req/s, error 0%.
- k6 script/result files:
  - K6/lms/scripts/04-admin-performance-load-test.js
  - K6/lms/results/04-admin-performance-course-summary.md
  - K6/lms/results/04-admin-performance-coupon-summary.md
- Screenshot evidence is outside repo: C:\Users\user\Desktop\모듈5_최적화.
- Important: monitoring/grafana/dashboards/algoga-lms.json was restored after measurement confusion; include it in a PR only if git status explicitly shows an intended dashboard change.
- Last observed git status showed only friend-domain files modified. Re-check status before any new work.

## Current Update 2026-07-26 Content Manager N+1 Fixes (Course Students / Reviews / Q&A)

- User asked to optimize slow admin-page queries ahead of a presentation: statistics manager (per-course interest, course->booking conversion, coupon->booking conversion) and content manager screens. User restricted edits to `course, country, enrollment, learningprogress, quiz, completion, certificate, review, qna, diagnosis, learning` packages only, and asked to be told (not have it silently fixed) if anything outside that list needed changing.
- A background Explore agent mapped every one of these features' full call chains. Result: all 3 statistics-manager features' real bottlenecks live in `stats`/`booking`/`benefit`/`accommodation` packages (outside the allowed list); content-manager screens (course students, reviews, Q&A) were fully fixable inside the allowed packages. Reported this to the user; they chose "fix content manager only for now."
- Implemented (see `.ai/WORKLOG.md` 2026-07-26 "Content Manager N+1 Query Optimization" entry for full file list):
  1. Admin course students list: was 6 DB queries per student; now 5 bulk queries per course page load (profile/progress/completion/quiz/review), enrollment reused instead of re-queried.
  2. Course review list/detail (admin + regular user list): was 1 profile query per review; now 1 bulk query per list call.
  3. Course Q&A list/detail: was 1 profile query per Q&A row, 1 per comment, plus 4x redundant repeated lookups of the same author in the detail view; now bulk-fetched.
- `compileJava` passes; `test` shows the same pre-existing/environment-only failures as before this change (verified via `git stash -u` comparison) — nothing in the touched packages regressed.
- **Not done, flagged to user instead of silently modified**: the 3 statistics-manager features. Their actual fix needs `stats` (all 3), plus `booking`/`accommodation` (course->booking conversion) and `benefit` (coupon->booking conversion). Full defect writeup (file:line, exact query problem) is in the WORKLOG entry above — reuse it directly if/when the user grants permission to touch those packages, rather than re-investigating from scratch.
- No load-test (k6/Grafana) evidence for this slice — user was short on time. Gave a lightweight, no-shared-file-edit alternative instead: SQL logging is already on globally (`logging.level.org.hibernate.SQL: debug` in `application.yaml`), so count `Hibernate:` log lines per request before/after; for exact counts, set `SPRING_JPA_PROPERTIES_HIBERNATE_GENERATE_STATISTICS=true` as a shell environment variable (not a file edit) before `bootRun`; use `curl -s -o /dev/null -w "%{time_total}s"` for wall-clock comparison.
- Branch note: `fix/admin-completion-rate-stats` was already checked out with unrelated prior work when this task started. These changes sit on top of it uncommitted — confirm with the user whether to commit into this branch or split into a new one before any PR.
