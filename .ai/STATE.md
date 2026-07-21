# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

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
