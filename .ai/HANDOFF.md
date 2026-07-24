# HANDOFF

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
