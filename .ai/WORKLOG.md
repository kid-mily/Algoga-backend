# WORKLOG

Record completed work here by date. Keep entries factual and useful for future debugging.

## Template

### YYYY-MM-DD Task Name

#### Summary

-

#### Changed Files

-

#### Verification

-

#### Problems

-

#### Resolution

-

#### Notes for Next Time

-

### 2026-07-24 LMS Dashboard Branch And Dead Code Cleanup

#### Summary

- Pulled latest `develop` with `git pull --ff-only` and created branch `refactor/lms-dead-code-and-performance-dashboard`.
- Preserved pre-existing local dashboard/QuizService edits in `stash@{0}` before switching branches.
- Restored only the Grafana dashboard change into the new branch; kept the prior `QuizService.java` local edit out of the current working tree.
- Removed confirmed dead code in Q&A: unused `CourseQnaComment.delete()`.
- No API URL/request/response/JSON contract changes.

#### Changed Files

- `monitoring/grafana/dashboards/algoga-lms.json`
- `src/main/java/com/kidmily/algoga_server/qna/domain/model/CourseQnaComment.java`
- `.ai/STATE.md`
- `.ai/HANDOFF.md`
- `.ai/WORKLOG.md`

#### Verification

- Dashboard JSON parsed with PowerShell `ConvertFrom-Json`.
- Confirmed dashboard panel IDs have no duplicates.
- `rg` found no remaining Q&A `CourseQnaComment.delete()` call sites.
- `./gradlew compileJava` passed.

#### Problems

- `C:\Algoga_V3_backend` required escalated git commands in this session because it was outside the current writable roots.
- PowerShell needed quoting for `stash@{0}` when restoring the dashboard file.

#### Resolution

- Used `stash@{0}` to preserve prior local edits and restored only the dashboard file into the new branch.
- Removed only the confirmed unused Q&A comment domain method; repository delete methods used by withdrawal listeners were left intact.

#### Notes for Next Time

- Recommended next step is to commit this small dashboard/dead-code slice before performance-code changes.
- Run Grafana/K6 baseline measurements before implementing public course list enrollment batch lookup and classroom Redis batch read.

### 2026-07-10 Course/LMS Refactor And Statistics Support

#### Summary

- Continued LMS/Course clean architecture refactor on `feature/course-clean-architecture-refactor`.
- Added statistics-manager support for course completion-rate calculation.
- Pushed the branch to `origin/feature/course-clean-architecture-refactor`.

#### Changed Files

- Course package refactor work completed in earlier commits:
  - Course/Chapter domain model and repository package split.
  - Course/Chapter persistence entity, Spring Data repository, adapter, mapper, S3 port/adapter, and presentation package split.
  - Chapter application command/result/usecase/service package split.
  - `CourseRepository` moved to `course.domain.repository`.
- Statistics support:
  - `src/main/java/com/kidmily/algoga_server/lms/domain/repository/CourseCompletionRepository.java`
  - `src/main/java/com/kidmily/algoga_server/lms/infrastructure/persistence/adapter/CourseCompletionRepositoryAdapter.java`
  - `src/main/java/com/kidmily/algoga_server/lms/infrastructure/persistence/repository/SpringDataCourseCompletionRepository.java`
- Follow-up import fix after latest `develop` merge:
  - `src/main/java/com/kidmily/algoga_server/booking/application/service/BookingCommandService.java`
- AI collaboration docs added/tracked:
  - `AGENTS.md`
  - `.ai/API.md`
  - `.ai/HANDOFF.md`
  - `.ai/STATE.md`
  - `.ai/WORKLOG.md`
  - `docs/ai/*`
  - `docs/design/figma-stats-manager-prompt.md`

#### Verification

- Ran `./gradlew clean compileJava` after adding `CourseCompletionRepository.countByCourseIds(...)`.
- Build passed with existing warnings.
- After a later `develop` pull, main compile hit an old `CourseRepository` import in booking code; fixed and pushed.

#### Problems

- Latest `develop` merges can introduce new imports to old LMS package paths while this branch is moving Course code into `course`.
- `docker-compose.yml` remains untracked and intentionally excluded.
- Git Bash/Windows produced CRLF warnings because global `core.autocrlf=true`.
- Docker Redis startup failed locally when Docker Desktop was not running.

#### Resolution

- Fixed the booking main-code import from `lms.domain.repository.CourseRepository` to `course.domain.repository.CourseRepository`.
- Kept `docker-compose.yml` out of the commit.
- Checked staged diffs before commit to avoid broad CRLF-only changes.

#### Notes for Next Time

- Before continuing refactor, search for old package references:
  - `rg "lms\.domain\.repository\.CourseRepository" src/main/java src/test/java`
- Booking main/test old `CourseRepository` imports were fixed and pushed. Continue checking old imports after each `develop` update.
- Continue refactor in small commits. Avoid moving all of `CourseService` at once because it touches payment, progress, quiz, review, Q&A, and completion behavior.

### 2026-07-10 Course Value Object Package Split

#### Summary

- Moved CourseLevel and CourseStatus from lms.domain.model to course.domain.model.
- Updated LMS service/result imports without changing API behavior.

#### Verification

- Pending final compile after CourseStatus move.
### 2026-07-11 MapRepository Country Package Split

#### Summary

- Moved MapRepository persistence boundary from `lms` to `country` package.
- Replaced `LmsCourseAdapter` MAP_REPOSITORY string/class lookup with typed `MapRepository` injection.
- Kept API request/response/json behavior unchanged.

#### Verification

- Ran `./gradlew clean compileJava`; build passed with existing warnings.
- Ran `./gradlew build -x test`; build passed.

### 2026-07-12 Course Application and Presentation Package Split

#### Summary

- Continued LMS package split on `refactor/lms-package-split-next`.
- Moved course-specific application command/result/cache classes from `lms` to `course`.
- Moved Course/Chapter admin request DTOs and Course/Chapter response DTOs from `lms.presentation` to `course.presentation`.
- Kept API URL, request fields, response fields, JSON structure, `ApiResponse`, and `PageResponse` unchanged.

#### Changed Scope

- `course/application/command`: `CreateCourseCommand`, `UpdateCourseCommand`
- `course/application/result`: `CourseResult`, `CourseFileResult`, `PublishedCourseListCacheResult`
- `course/application/service`: `PublishedCourseListCacheService`
- `course/presentation/request/admin`: Course/Chapter create/update request DTOs
- `course/presentation/response`: Course/Chapter list/admin/file response DTOs
- Updated imports in existing LMS/diagnosis/course controllers and services.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseUseCase` and `CourseService` remain under `lms` because they still include enrollment, completion, Q&A, classroom, and diagnosis-related behavior.
- Next likely slice: split `CourseUseCase` responsibilities or move course-only service logic carefully without changing API contracts.

### 2026-07-12 Course Cache Settings Split

#### Summary

- Moved public course list cache settings from `lms.settings.cache` to `course.settings.cache`.
- Renamed `LmsCacheType`/`LmsCacheRegistry` to `CourseCacheType`/`CourseCacheRegistry`.
- Updated course cache usage in `PublishedCourseListCacheService` and cache eviction usage in `CourseService`.
- Kept cache name and TTL unchanged: `lmsPublicCourseList`, `10 * 60` seconds.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

### 2026-07-12 LearningProgress Package Split

#### Summary

- Moved learning progress command, port, result, service, usecase, scheduler, domain model/repository, persistence, Redis adapter, controller, request, and response classes from `lms` to `learningprogress`.
- Preserved learning progress API path, request fields, response fields, Redis write-behind behavior, cache key behavior, and JSON structure.
- Updated dependent imports in `CourseService`, `QuizService`, and `CourseServiceClassroomTest`.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `LearningProgressResult` still references LMS classroom result types because classroom/course service responsibilities are not fully split yet.
- Next likely slice: split `Enrollment` package or isolate course classroom/service boundaries before moving more CourseService responsibilities.

### 2026-07-12 Enrollment Package Split

#### Summary

- Moved enrollment event listener, domain model, repository port, persistence adapter/entity/repository from `lms` to `enrollment` package.
- Updated dependent imports in benefit LMS adapter, learning progress service, LMS course/quiz services, stats interest service, and related tests.
- Kept API URL, request fields, response fields, JSON structure, and enrollment behavior unchanged.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` still owns several LMS classroom/course flows and now imports the enrollment port explicitly.
- Next likely slice: split quiz or review/Q&A package after committing this Enrollment slice.

### 2026-07-12 Quiz Package Split

#### Summary

- Moved quiz command, result, usecase, service, domain model/repository, persistence adapter/entity/repository, controller, request, and response classes from `lms` to `quiz` package.
- Updated dependent imports in `CourseService`, `CourseServiceClassroomTest`, and benefit LMS adapter.
- Preserved quiz API URLs, request fields, response fields, JSON structure, and behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `QuizSubmitResult` and `QuizSubmitResponse` still depend on LMS completion result/response types because completion has not been split yet.
- Next likely slice: split completion/certificate or review/Q&A, depending on desired PR size.

### 2026-07-12 Completion and Certificate Package Split

#### Summary

- Moved course completion result, domain model/repository, persistence adapter/entity/repository, controller, and response classes from `lms` to `completion` package.
- Moved certificate PDF service and certificate download controller from `lms` to `certificate` package.
- Updated dependent imports in benefit, booking, LMS course/review services, quiz, stats, and related tests.
- Preserved API URLs, request fields, response fields, PDF download behavior, JSON structure, and completion reward event behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` still owns the completion workflow method and imports completion package types explicitly.
- Next likely slice: split review/Q&A or diagnosis package.

### 2026-07-12 Review and Q&A Package Split

#### Summary

- Moved course review command, result, usecase, service, scheduler, domain model/repository, persistence adapter/entity/repository, controller, request, and response classes from `lms` to `review` package.
- Moved course Q&A command, result, domain model/repository, persistence adapter/entity/repository, controller, request, and response classes from `lms` to `qna` package.
- Updated dependent imports in LMS course usecase/service and classroom test.
- Preserved API URLs, request fields, response fields, JSON structure, review deletion scheduling behavior, and Q&A behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` and `CourseUseCase` still own course classroom/Q&A orchestration and import review/qna package types explicitly.
- Next likely slice: split diagnosis package, then decide whether to separate remaining LMS classroom/course orchestration further.
### 2026-07-12 Diagnosis Package Split

#### Summary

- Moved diagnosis command, result, usecase, service, domain model/repository, persistence adapter/entity/repository, controller, request, response, and related service test classes from `lms` to `diagnosis` package.
- Updated dependent imports and removed obsolete LMS domain wildcard imports from `CourseService`.
- Preserved diagnosis API URLs, request fields, response fields, JSON structure, and diagnosis result persistence behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `DiagnosisService` still depends on `CourseUseCase`, `MapRepository`, and `UserProfilePort` as before; only package boundaries changed.
- Next likely slice: split remaining classroom/course orchestration pieces or start file upload constraints, depending on PR size.
### 2026-07-12 Course Statistics Package Split

#### Summary

- Moved course enrollment statistics port, result, usecase, service, query repository, admin controller, and response classes from `lms` to `course.statistics` package.
- Preserved admin statistics API URL, request parameters, response fields, JSON structure, and authorization condition.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- This split keeps course-specific statistics separate from the broader `stats` manager package.
- Next likely slice: decide whether to split map/my-course/student classroom APIs or move to file upload constraints.
### 2026-07-12 Course Classroom/MyCourse Package Split

#### Summary

- Moved my-course, classroom, and course-student result classes from `lms` to `course.application.result`.
- Moved my-course, classroom, and course-student response/controller classes from `lms` to `course.presentation`.
- Updated dependent imports in `CourseService`, `CourseUseCase`, learning progress flow, and classroom test.
- Preserved API URLs, request parameters, response fields, JSON structure, and authorization behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` still owns the actual orchestration logic for my-course/classroom/student queries.
- Next likely slice: split map presentation/application pieces or decide whether to extract `CourseService` orchestration into smaller services.
### 2026-07-12 Map/Country Package Split

#### Summary

- Moved map application result/usecase/service classes from `lms` to `country.application`.
- Moved map controller and country/continent response classes from `lms` to `country.presentation`.
- Removed obsolete LMS application result wildcard imports from `CourseService` and `CourseUseCase`.
- Preserved `/api/v1/maps` API URLs, response fields, JSON structure, and course-count aggregation behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `MapController` still depends on `CourseUseCase` for published-course counts by country, matching existing behavior.
- Next likely slice: course file/storage package remnants or careful extraction of `CourseService` orchestration.
### 2026-07-12 Course File/Storage Package Split

#### Summary

- Moved course storage settings from `lms.settings.LmsStorageSettings` to `course.settings.CourseStorageSettings`.
- Moved `CourseFileJpaEntity` to `course.infrastructure.persistence.entity`.
- Moved `LocalFileStorageManager` to `course.infrastructure.document`.
- Updated dependent imports in course services, mapper/entity, and classroom test.
- Preserved S3 bucket name, storage directory values, API URLs, request fields, response fields, and JSON structure.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- LMS exception classes still remain under `lms.exception` for now and are referenced by course file storage code.
- Next likely slice: split remaining course command/usecase/port pieces or decide whether to extract `CourseService` orchestration.
### 2026-07-12 Course UseCase/Command Package Split

#### Summary

- Moved `CourseUseCase` from `lms.application.usecase` to `course.application.usecase`.
- Moved `CompleteCourseCommand` from `lms.application.command` to `course.application.command`.
- Updated dependent imports in course, completion, country, diagnosis, Q&A controllers/services, and diagnosis service test.
- Preserved API URLs, request fields, response fields, JSON structure, and business behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` still remains under `lms.application.service` but now implements the course package use case.
- Next likely slice: move `CourseService` itself or split `UserProfilePort`/`UserProfileAdapter` before moving the service.
### 2026-07-12 Course UserProfile Port Package Split

#### Summary

- Moved LMS `UserProfilePort` to `course.application.port`.
- Moved LMS `UserProfileAdapter` to `course.infrastructure.user`.
- Updated dependent imports in course, diagnosis, Q&A, review services/results, and related tests.
- Kept the adapter bean name unchanged to minimize hidden wiring impact.
- Preserved API URLs, request fields, response fields, JSON structure, and behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `benefit.application.port.UserProfilePort` remains separate and unchanged.
- Next likely slice: move `CourseService` to `course.application.service` after checking remaining LMS exception/support dependencies.
### 2026-07-12 CourseService Package Split

#### Summary

- Moved `CourseService` from `lms.application.service` to `course.application.service`.
- Updated `CourseServiceClassroomTest` to import the moved service.
- Preserved API URLs, request fields, response fields, JSON structure, cache behavior, and business behavior.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

#### Notes

- `CourseService` still uses `lms.exception` until shared exception package handling is decided.
- Remaining LMS package files are mostly exception/support/config/test remnants.
### 2026-07-12 LMS Residual Cleanup

#### Summary

- Pulled latest `develop` and created branch `refactor/lms-cleanup-architecture`.
- Removed the remaining Java `lms` package from main/test source paths.
- Renamed shared learning exceptions from `LmsErrorCode`/`LmsException` to `LearningErrorCode`/`LearningException` under `learning.exception`.
- Moved exception advice to `learning.presentation.advice` and expanded its controller package scope to the split learning domains.
- Moved `CurrentUserIdResolver` to `learning.presentation.support`.
- Removed duplicate `LmsSchedulingConfig` because `global.config.SchedulerConfig` already enables scheduling.
- Moved course-related tests from `lms` test packages to `course` test packages.
- Moved LMS HTTP request samples from `src/main/java` to `docs/http/learning`.
- Preserved error code values such as `LMS_001`, API URLs, request fields, response fields, and JSON structure.

#### Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.
- `./gradlew testClasses` passed.

### 2026-07-13 CourseService Responsibility Split And Q&A Domain Move

#### Summary

- Split `CourseService` responsibilities into focused collaborators and moved Q&A ownership into the `qna` domain, on branch `refactor/lms-cleanup-architecture`. PR merged into `develop`.
- API URLs, request fields, response fields, JSON structure, and `LMS_###` error code values unchanged.

#### Changed Files

- New (course.application.service): `CourseFileManager`, `CourseProgressCalculator`, `CourseProgressReader`, `CourseStudentResultAssembler`, `MyCourseResultAssembler`.
- New (qna): `qna.application.usecase.CourseQnaUseCase`, `qna.application.service.CourseQnaService`.
- Modified: `course.application.service.CourseService`, `course.application.usecase.CourseUseCase`, `qna.presentation.api.CourseQnaController`, `qna.presentation.api.admin.AdminCourseQnaController`, `course.application.service.CourseServiceClassroomTest`.
- Dead code: `course.infrastructure.document.LocalFileStorageManager` commented out (superseded by S3).
- Unrelated author edits pushed in the same PR: `benefit/UserCoupon`, `payment/PaymentQueryService`, `payment/PaymentTransactionService`.

#### Verification

- `./gradlew compileJava` passed with existing warnings.
- `./gradlew test --tests "*CourseServiceClassroomTest"` passed.

#### Problems

- Local working tree showed ~1,295 CRLF-only modified files (global `core.autocrlf`); committed only the intended files explicitly to avoid whitespace-only noise.
- A stale `.git/index.lock` appeared after a shell error (PowerShell backtick continuation run in Git Bash); removed with `rm -f .git/index.lock`.

#### Resolution

- Staged specific file paths (not `git add .`), keeping the CRLF noise and unrelated files out of the intended scope.
- Re-wired `CourseServiceClassroomTest` mocks for the new constructor: added `CourseFileManager` and `CourseProgressReader` mocks and stubbed `CourseProgressReader.loadProgressMapWithCache`.

#### Notes for Next Time

- Relocated/dead code is intentionally kept as comments (reason-tagged) for history; delete in a final cleanup slice once stabilized.
- Dependency direction is now `qna -> course` (course no longer depends on qna).
- Next likely slices: extract `completeCourse` completion validators/policy and `getCourseClassroom` chapter-locking assembly; optionally other large services (`QuizService`, `DiagnosisService`, `CertificatePdfService`).

### 2026-07-14 Service SRP Decomposition (Second Wave) And Convention Fixes

#### Summary

- Continued SRP decomposition across the remaining large LMS services and applied two convention fixes. API URLs/request/response/JSON and `LMS_###` error codes unchanged. Currently uncommitted.

#### Changed Files

- Course: new `course.application.policy.CourseCompletionPolicy`, `course.application.service.CourseClassroomAssembler`; `CourseService.completeCourse`/`getCourseClassroom` delegate.
- Quiz: new `quiz.application.policy.QuizAccessPolicy`, `quiz.application.service.QuizGrader`, `quiz.application.service.QuizInputValidator`; `QuizService` delegates.
- Diagnosis: new `diagnosis.application.service.DiagnosisGrader`, `DiagnosisInputValidator`; `DiagnosisService` delegates.
- Review: new `review.application.service.ReviewRatingSummaryCalculator`; `CourseReviewService.getReviewSummary` delegates.
- LearningProgress: `createClassroomResult` reuses `CourseClassroomAssembler`; new `learningprogress.application.service.LearningProgressCalculator`.
- Shared: new `completion.application.service.CourseCompletionRegistrar` (used by `CourseService.completeCourse` and `QuizService.submitQuiz`, replacing duplicated completion creation).
- #1: `CertificateController` package `completion.presentation.api` -> `certificate.presentation.api`; new `certificate.application.usecase.CertificateUseCase` (implemented by `CertificatePdfService`); controller injects the use case.
- #2: `CreateChapterCommand`/`UpdateChapterCommand` `MultipartFile` -> `UploadFile`; `AdminChapterController.toUploadFile` conversion; `ChapterService` uses `CourseFileStoragePort`.

#### Verification

- Per-slice `./gradlew compileJava` passed. `*CourseServiceClassroomTest` and `*DiagnosisServiceTest` passed. Other services compile-verified (no dedicated tests).

#### Problems / Notes

- `ChapterService` port switch (`global` `FileStoragePort` -> `course` `CourseFileStoragePort`) is safe because both S3 adapters generate identical keys (`directory/UUID.ext`), same bucket, same exception -> response `videoUrl` unchanged.
- `CertificatePdfService` left as-is (single responsibility; length is just PDF-drawing primitives).
- #3/#4 (cross-domain ports/adapters for `CourseCompletionPolicy`/`QuizAccessPolicy`) deferred by decision: over-engineering for one LMS module sharing a datastore.

#### Notes for Next Time

- Before deleting commented-out code, run a full `./gradlew build` once as a safety gate.
- New pure utils (Grader/Calculator/Assembler) are ideal cheap unit-test targets.

#### Notes

- Java source/test references to `com.kidmily.algoga_server.lms` were removed.
- The remaining `LMS_###` strings are API error code values and were intentionally preserved for frontend/API compatibility.

### 2026-07-21 LMS Domain Concurrency And Validation Bug Fixes

#### Summary

- Branch `fix/lms-domain-concurrency-and-validation-bugs` (off `develop`, after the separate `fix/user-withdrawal-cascade` deletion-policy work had already merged).
- Fixed 6 correctness bugs found via a code-review pass across `course`, `country`, `enrollment`, `learningprogress`, `quiz`, `completion`, `certificate`, `review`, `qna`, `diagnosis`. No API URL/request/response/JSON changes in any of these.

#### Changed Files

1. **Diagnosis partial-answer scoring bug** — a submission covering only some of a country's active questions could still be graded and scored 100%.
   - `diagnosis/application/service/DiagnosisInputValidator.java`: added `validateAnswerCoverage(answers, activeQuestions)`.
   - `diagnosis/application/service/DiagnosisService.java`: calls it in `submitResult` before grading.
   - Test: `diagnosis/application/service/DiagnosisServiceTest.java` (new case).

2. **Quiz retake policy + submission race (500 error)** — quiz is now strictly one-attempt-only; duplicate/concurrent submission is rejected instead of silently overwriting the prior result.
   - `quiz/exception/QuizErrorCode.java`: added `QUIZ_ALREADY_SUBMITTED` (409, `LMS_044`).
   - `quiz/application/service/QuizService.java`: `submitQuiz` checks `existsByUserIdAndCourseId` up front and throws before grading.
   - `quiz/infrastructure/persistence/adapter/QuizSubmissionRepositoryAdapter.java`: removed the old "update existing submission" path entirely; `save()` now always attempts a fresh `saveAndFlush()` insert in the *same* ambient transaction (no `REQUIRES_NEW` — deliberately, so a later failure in saving submission-answers rolls the submission back too) and translates `DataIntegrityViolationException` into `QUIZ_ALREADY_SUBMITTED`.
   - `quiz/application/service/QuizServiceTest.java`, `quiz/infrastructure/persistence/adapter/QuizSubmissionRepositoryAdapterTest.java`: updated/new cases.
   - Note: a `QuizSubmissionInsertTransactionExecutor` (REQUIRES_NEW) was added then removed again in the same session after a review caught that isolating the submission insert from the ambient transaction could let `quiz_submission` commit independently while a later `quiz_submission_answer` write fails, leaving a submitted-with-no-answers state that (given the no-retry policy) the user could never fix.

3. **Certificate code collision crash** — a random 6-digit `certificate_code` collision caused an unhandled 500 for a legitimately-completing user.
   - `completion/application/service/CourseCompletionRegistrar.java`: retries up to 5x with a fresh code on `DataIntegrityViolationException`; on a concurrent-completion race (loses to another request), returns the winner's row without re-publishing `CourseCompletionCompletedEvent` (`freshlyCreated` flag) to avoid double-granting rewards.
   - `completion/application/service/CourseCompletionInsertTransactionExecutor.java` (new): `insertNew`/`findExisting` each `@Transactional(REQUIRES_NEW)`, so a failed attempt doesn't poison the persistence context for the next retry or the caller's transaction.
   - `completion/infrastructure/persistence/adapter/CourseCompletionRepositoryAdapter.java`: `save()` uses `saveAndFlush()` so the unique-constraint violation surfaces synchronously inside the `REQUIRES_NEW` attempt.
   - Test: `completion/application/service/CourseCompletionRegistrarTest.java` (new, 3 cases).
   - **Known deferred issue**: `CourseCompletionInsertTransactionExecutor`'s `REQUIRES_NEW` commits the `CourseCompletion` row independently of the caller's (`QuizService`/`CourseService`) ambient transaction. If that ambient transaction fails at final commit (rare — connection loss, commit-time lock/deadlock, etc.) after `register()` returns, the `CourseCompletion` row stays committed while the quiz submission/answers roll back and the `AFTER_COMMIT` reward/notification events never fire (and never will, since a retry finds the already-existing completion and skips re-publishing). Narrow window, not fixed in this session — needs a design decision (candidates discussed: `Propagation.NESTED` savepoint instead of `REQUIRES_NEW`; a near-collision-free code generation scheme so retries are effectively never needed; or accepting a single ambient transaction and simplifying the collision handling). Do not "fix" by just removing `REQUIRES_NEW` here the way it was removed for quiz — the retry-up-to-5-times design genuinely needs isolated attempts, unlike quiz's single-shot reject.

4. **Learning progress lost-update race** — concurrent writes to the same chapter's Redis-cached `watchedSeconds` could regress the value (violating "progress never decreases").
   - `learningprogress/infrastructure/redis/LearningProgressRedisAdapter.java`: `write()` now goes through an atomic Lua compare-and-set script (skips the write if the currently-cached `watchedSeconds` is already higher) instead of a blind `opsForValue().set(...)`.
   - Test: `learningprogress/infrastructure/redis/LearningProgressRedisAdapterTest.java` (new; verifies the adapter calls the atomic script with the right key/args — cannot verify the Lua script's own runtime behavior in this environment since no Redis is available; recommend a manual/integration check against real Redis before considering this fully verified).

5. **QnA reply to a soft-deleted parent comment** — replying to a hidden parent comment produced an orphaned reply (parent absent from the thread, child still returned).
   - `qna/application/service/CourseQnaService.java`: `validateParentComment` now also rejects `parentComment.isDeleted()`.
   - Test: `qna/application/service/CourseQnaServiceTest.java` (new, 2 cases).

6. **Course review resubmission blocked after admin hides it (500 error)** — `course_reviews` has a `(user_id, lecture_id)` unique constraint not scoped by `deleted`, but `createReview` only checked `existsByUserIdAndCourseIdAndDeletedFalse` (non-hidden only). After an admin hides a user's review, that user's next review attempt passed the app-level check, then hit the DB unique constraint and surfaced as a raw 500, permanently (until the 14-day `CourseReviewDeletionScheduler` purge).
   - Decision (confirmed with product owner): a hidden review still counts as "already reviewed" — no rewrite allowed at all, not even to a fresh row. (The alternative — auto-restore/overwrite the hidden row with the new content — was considered and rejected.)
   - `review/application/service/CourseReviewService.java`: `createReview`'s duplicate check changed from `existsByUserIdAndCourseIdAndDeletedFalse` to `findByUserIdAndCourseId(...).isPresent()` (deleted-status-agnostic), throwing the existing `REVIEW_ALREADY_EXISTS` (409) cleanly instead of reaching the DB insert.
   - `course/application/service/CourseStudentResultAssembler.java`'s own use of `existsByUserIdAndCourseIdAndDeletedFalse` (admin "did this student write a review" display) was intentionally left unchanged — different purpose (visible-review display, not a write-path duplicate check).
   - Test: `review/application/service/CourseReviewServiceTest.java` (new).

#### Verification

- `./gradlew compileJava` / `compileTestJava` passed after each change.
- `./gradlew test` — 159 tests, 9 failing, all pre-existing and unrelated (DB/Redis-dependent Spring context-loading tests with no local DB/Redis in this environment, plus one already-broken `CourseRewardServiceTest`) — confirmed pre-existing by running the same suite with `git stash -u` before these changes.

#### Notes for Next Time

- Item 3's deferred atomicity gap (`CourseCompletionRegistrar` vs. the caller's ambient transaction) is the main open design question from this session — see the "Known deferred issue" note above before touching that file again.
- Nothing in this branch has been committed yet as of this entry.
### 2026-07-25 Module 5 Presentation Seed SQL

#### Summary

- Added presentation seed SQL and matching rollback SQL for manager-side demo data without changing API URL/request/response/JSON contracts or application code.
- Files:
  - `db/seed/module5-demo-data.sql`
  - `db/seed/module5-demo-data-rollback.sql`

#### Coverage

- Seed covers realistic Korean data for users, referral rewards, friend relations, coupon policies/user coupons, mileage histories, lecture/travel payments, enrollments, learning progress, quiz submissions/answers, course completions/rewards, reviews, QnA/comments, diagnosis results, bookings, and refund requests.
- Existing lectures/chapters/quizzes/countries/accommodations/packages/managers are reused through SQL lookups/temporary tables instead of hard-coded PKs.
- Seeded user password is `password123`; the SQL stores a BCrypt hash generated with Spring Security `BCryptPasswordEncoder` and verified with `matches("password123") == true`.
- User-facing text avoids `demo`; rollback identification uses technical `M5` keys such as seeded emails, payment idempotency keys, booking numbers, and certificate codes.

#### Verification

- Reviewed entity/table/enum values in code before writing SQL.
- Checked for leftover obvious camelCase native SQL columns and `demo` text in seed contents.
- No local MySQL instance was available, so the SQL has not been executed against a real database yet.
