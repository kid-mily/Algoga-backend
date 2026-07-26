# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

## Current Update 2026-07-25

- Added Module 5 presentation seed SQL: `db/seed/module5-demo-data.sql` and `db/seed/module5-demo-data-rollback.sql`.
- Scope: SQL-only data setup for manager-side presentation. No Java code changes and no API URL/request/response/JSON contract changes.
- Seed uses existing DB rows through lookup/temporary tables for lectures, chapters, quizzes, countries, accommodations, packages, and managers rather than hard-coded PKs.
- Seeded users use login password `password123` with a Spring Security BCrypt hash. User-facing contents are natural Korean data and avoid `demo`; technical `M5` identifiers are used only for rollback-safe keys.
- Verification: code/table/enum review and static SQL text checks were done. SQL has not been run against a real MySQL database in this environment.

## Current Update 2026-07-24

- Branch `refactor/lms-dead-code-and-performance-dashboard` was created from latest `develop` after `git pull --ff-only` (local `develop` was 22 commits behind).
- Current goal: prepare the LMS Grafana performance dashboard for before/after measurement and remove confirmed dead code in the LMS-adjacent domains without changing API URL/request/response/JSON contracts.
- Local changes that existed before branching were saved in `stash@{0}`. Only `monitoring/grafana/dashboards/algoga-lms.json` was restored into this branch; the prior `quiz/application/service/QuizService.java` local change remains preserved in the stash and is not in the current working tree.
- Completed so far: restored the dashboard change, removed unused `CourseQnaComment.delete()`, verified dashboard JSON/panel IDs, and ran `./gradlew compileJava` successfully.
- Next: commit the dashboard/dead-code cleanup if desired, then run Grafana/K6 baseline measurements before implementing public-course-list enrollment batch lookup and classroom Redis batch read.

## Current Work

- Branch `fix/lms-domain-concurrency-and-validation-bugs` (off `develop`). Not committed yet.
- Goal: fix a set of correctness bugs (validation gaps, concurrency races, a duplicate-submission policy gap) found via a code-review pass across the LMS-adjacent domains (`course`, `country`, `enrollment`, `learningprogress`, `quiz`, `completion`, `certificate`, `review`, `qna`, `diagnosis`), without changing any API URL/request/response/JSON contract.
- The earlier `CourseService` responsibility-split refactor (2026-07-10 through 2026-07-14, tracked below under "Earlier completed work") is done and merged into `develop`; it is unrelated to the current branch.
- A separate, already-merged branch (`fix/user-withdrawal-cascade`) added user-withdrawal cascade-delete listeners across these same domains plus `benefit`/`friend`/`chat` and fixed a mileage-expiration-at-checkout bug; not tracked in detail here since it's already merged.

## Progress (current branch)

- Fixed and tested (see `.ai/WORKLOG.md` 2026-07-21 entry for full detail):
  1. Diagnosis: reject submissions that don't cover every active question (previously any subset could be graded/scored).
  2. Quiz: enforced one-attempt-only — reject duplicate/concurrent submission (`QUIZ_ALREADY_SUBMITTED`, 409) instead of silently overwriting the prior result; removed the old update-on-resubmit path in `QuizSubmissionRepositoryAdapter`.
  3. Certificate: retry certificate-code generation on unique-constraint collision (up to 5x) via an isolated `REQUIRES_NEW` executor bean; avoid double-publishing the completion-reward event when losing a concurrent-completion race.
  4. Learning progress: Redis write path now uses an atomic Lua compare-and-set so concurrent writes can't regress `watchedSeconds`.
  5. QnA: reject replies to a soft-deleted parent comment.
  6. Course review: a hidden (admin-moderated) review now counts as "already reviewed" — resubmission is rejected with `REVIEW_ALREADY_EXISTS` (409) instead of crashing with an unhandled DB unique-constraint violation (500).
- Known, deliberately deferred issue (not a bug in this branch's scope, but found and discussed while fixing #3): `CourseCompletionRegistrar`'s `REQUIRES_NEW`-based certificate-retry can commit the `CourseCompletion` row independently of the caller's (`QuizService`/`CourseService`) transaction. If that caller transaction fails at final commit after `register()` returns (rare), the completion row survives while the quiz submission/answers roll back and the reward/notification event never fires (and a retry won't re-fire it either, since it finds the existing completion). See the WORKLOG entry's "Known deferred issue" note for candidate fixes (NESTED savepoint / near-collision-free code generation / single-transaction simplification) before touching this.

## Earlier completed work (superseded, kept for context)

- `CourseService` responsibility-split refactor completed on branch `refactor/lms-cleanup-architecture` and merged into `develop` (2026-07-13). Extracted collaborators (`CourseFileManager`, `CourseProgressCalculator`, `CourseProgressReader`, `CourseStudentResultAssembler`, `MyCourseResultAssembler`), moved Q&A ownership to `qna`.
- Second SRP-decomposition wave (`CourseCompletionPolicy`, `CourseClassroomAssembler`, `QuizAccessPolicy`, `QuizGrader`, `QuizInputValidator`, `DiagnosisGrader`, `DiagnosisInputValidator`, `ReviewRatingSummaryCalculator`, `LearningProgressCalculator`, `CourseCompletionRegistrar`, Certificate/Chapter convention fixes) — status of that wave's own commit/merge was last recorded as "pending" on 2026-07-14; not verified as part of this entry's update. Check `git log` on `develop` if this matters for new work.
- Full package split history (course/country/enrollment/learningprogress/quiz/completion/certificate/review/qna/diagnosis split out of the old `lms` package) — see `.ai/WORKLOG.md` entries from 2026-07-10 through 2026-07-14 for the full slice-by-slice history.

## Latest Verification

- `./gradlew compileJava` / `compileTestJava` pass.
- `./gradlew test`: 159 tests, 9 pre-existing/unrelated failures (DB/Redis-dependent Spring context tests — no local DB/Redis in this environment — plus one already-broken `CourseRewardServiceTest`). Confirmed pre-existing via `git stash -u` + re-run before making these changes.
- The Lua compare-and-set script (item 4) is only verified at the "adapter calls the script with the right args" level in this environment (no Redis available); its actual runtime CAS behavior should be checked against a real Redis before treating it as fully verified.

## Next Steps

1. Decide on and implement a fix for the deferred `CourseCompletionRegistrar` atomicity gap (see above), or explicitly accept the risk.
2. Commit and push this branch, open a PR.
3. Manually/integration-verify the Redis Lua CAS script against a real Redis instance.
4. A broader, not-yet-scheduled list of optimization/cleanup candidates (N+1 queries, missing caching on read-heavy endpoints, a duplicated `CurrentUserIdResolver` across ~10 packages, a few dead-code items) was identified during the review passes in this session but intentionally not implemented — ask before picking any of these up, since they're optimization/cleanup, not bug fixes.

## Cautions

- Do not change API contracts unless explicitly requested.
- Preserve `LMS_###` error code values for frontend/API compatibility.
- Avoid broad formatting, import-order, or whitespace-only changes.
- Do not "fix" the deferred `CourseCompletionRegistrar` issue by simply removing `REQUIRES_NEW` (that reintroduces the certificate-code-collision crash it was added to fix) — it needs an actual design decision.
- This environment has no local MySQL/Redis; only unit tests with mocks can be run here. Anything concurrency/DB-constraint-timing-sensitive (items 2-4 above) would benefit from a real-infra check before merging.

## Current State 2026-07-26

- Admin LMS course/coupon optimization and k6/Grafana measurement work has been summarized in .ai/WORKLOG.md and .ai/HANDOFF.md.
- Before continuing, check actual branch and dirty files with git status in C:\Algoga_V3_backend because the working tree changed several times during PR preparation.
- Last observed dirty files were friend-domain files only, not LMS optimization files.

## Current State 2026-07-26 (Content Manager N+1 Fixes)

- On branch `fix/admin-completion-rate-stats` (pre-existing, unrelated prior work on that branch). Not committed yet.
- Scope constraint from user: only `course, country, enrollment, learningprogress, quiz, completion, certificate, review, qna, diagnosis, learning` packages may be modified for this task.
- Fixed 3 N+1 query problems in admin Content Manager screens (see `.ai/WORKLOG.md` 2026-07-26 "Content Manager N+1 Query Optimization" entry for full file-level detail): admin course students list (6 queries/student -> 5 bulk queries/course), course review list/detail (1 profile query/review -> 1 bulk query), course Q&A list/detail (1 profile query/row + 1/comment + 4x redundant -> bulk).
- Stats Manager 3 features (강의별 관심도 / 강의->예약 전환 / 쿠폰->예약 전환) were investigated but **intentionally not modified** — their real bottlenecks live in `stats`, `booking`, `benefit`, `accommodation` packages, all outside the allowed scope. User was informed and chose "content manager only for now." Full defect detail in WORKLOG.
- `./gradlew compileJava` passes. `./gradlew test`: 221 tests, 12 failed, all confirmed pre-existing/environment-only (verified via `git stash -u` re-run before these changes) — none in the touched packages' own test classes.
- No k6/Grafana before/after measurement was done for this slice (time-boxed by user). Quick-measurement alternatives (no shared-config-file edits needed) were given to the user in-chat and recorded in WORKLOG.

## Current State 2026-07-26 (Stats Manager Work — Superseded/Discarded)

- A separate `refactor/admin-stats-manager-query-optimization` branch (fixing 강의별 관심도/강의→예약 전환/쿠폰→예약 전환 in `stats`/`booking`/`benefit`) was built earlier today but **discarded and deleted** before being committed — a teammate independently fixed and merged an equivalent stats-manager optimization directly into `develop` (commits `4c0c73b`/`ee20307`, "[Perf] 통계매니저 조회 3종 최적화"). Confirmed redundant by the user; no action needed on stats manager going forward unless something new comes up.

## Current State 2026-07-26 (Content Manager Pagination Unification + Mileage Fix)

- New branch `refactor/admin-content-manager-pagination-unification`, created fresh from current `develop` (which now includes both the earlier content-manager fix and the teammate's stats-manager fix). Not committed yet.
- User expanded scope for this branch to include `benefit` (mileage + coupon-management screens, both shown under the "콘텐츠 매니저" sidebar), explicitly excluding `packages` (패키지 관리, not the user's part).
- Work done (full detail in `.ai/WORKLOG.md` 2026-07-26 "Content Manager Pagination Unification + Mileage N+1/Full-Scan Fix" entry):
  1. Mileage (`benefit`): fixed the same `findAll()` full-scan + N+1 pattern as the earlier coupon bug (never fixed until now) — added bulk profile lookup, bulk history fetch, and a DB-side global-totals aggregate; both mileage endpoints now paginated.
  2. Unified pagination to **10 items per page** across mileage, Q&A (public + admin), review (public + admin), diagnosis admin results, and course admin list (the last one already had `Pageable` but relied on Spring's undocumented default of 20).
  3. Diagnosis admin results: fixed a genuine full-table-scan that occurred whenever an admin loaded the results screen with no filters (the natural default view).
- This is a real API contract change (5 endpoints: mileage summary, mileage histories, Q&A list, review list, diagnosis results — responses that were bare arrays are now `PageResponse`-wrapped, or nest a `PageResponse` field for mileage summary). User was informed and accepted the frontend impact; a frontend handoff message was drafted in-chat.
- `compileJava`/`test` pass; same 7 pre-existing/environment-only failing test classes as every other verification pass today — nothing new broken. Mileage has no test coverage at all (pre-existing gap, not introduced here).
- Not verified against real MySQL in this environment — the new mileage aggregate query (`findGlobalTotals`) and paginated distinct-user query should get a manual check before merging.

## Current State 2026-07-26 (Unified Admin List APIs)

- Same branch (`refactor/admin-content-manager-pagination-unification`). Frontend formally requested 4 new cross-course admin list endpoints (quiz/coupon-policy/qna/review) to replace a 1+N-request-per-screen pattern, plus quiz min/max count validation, plus package-list country/accommodation enrichment.
- Implemented all 4 new endpoints (additive, existing per-course endpoints untouched) + quiz min-count-on-delete validation (`QUIZ_MIN_COUNT_REQUIRED`, LMS_045). Full detail in `.ai/WORKLOG.md` 2026-07-26 "Unified Admin List APIs" entry.
- **Did not implement** the package-list `countryName`/`accommodationName` ask — `packages` domain is explicitly out of scope per the user's earlier instruction. Flagged back to user, not silently done.
- **Discovered, not fixed**: quiz delete claims "soft delete" in its doc comment and the domain model even has a `deleted`/`softDelete()` field/method, but the actual adapter code does a real hard delete. Needs a product decision (FK implications with `quiz_submission_answer`) before touching.
- `compileJava`/`test` pass; same 7 pre-existing/environment-only failing classes as every check this session, nothing new broken. No test coverage exists yet for `deleteQuiz`'s new min-count check.
- Not verified against real MySQL — none of the 4 new `searchForAdmin` JPQL queries have been run against real data here.
