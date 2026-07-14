# HANDOFF

## Current Goal

- Reduce CourseService size/coupling by extracting collaborators and move Q&A ownership to the `qna` domain, without changing API contracts.

## Completed Recently

- CourseService responsibility-split slice completed on `refactor/lms-cleanup-architecture` and its PR merged into `develop` (2026-07-13).
- Included some unrelated author edits in the same push: `benefit/UserCoupon`, `payment/PaymentQueryService`, `payment/PaymentTransactionService`.

## Current Slice Details

- Extracted collaborators from `CourseService` (all in `course.application.service` unless noted):
  - `CourseFileManager` (file/thumbnail upload/delete).
  - `CourseProgressCalculator` (pure calculations, static util).
  - `CourseProgressReader` (progress DB/cache read + merge).
  - `CourseStudentResultAssembler` (admin course-student result).
  - `MyCourseResultAssembler` (my-course result).
- Moved Q&A use case to the `qna` domain:
  - Added `qna.application.usecase.CourseQnaUseCase` and `qna.application.service.CourseQnaService`.
  - Repointed `CourseQnaController` and `AdminCourseQnaController` from `CourseUseCase` to `CourseQnaUseCase`.
  - Commented out the Q&A methods/helpers/fields in `CourseUseCase`/`CourseService`.
  - Dependency direction is now `qna -> course`; `course` no longer depends on `qna`.
- Commented out dead `LocalFileStorageManager` (superseded by S3).
- Relocated/removed code is commented out (not deleted) with reason comments. `CourseServiceClassroomTest` mocks were re-wired to match the new constructor.
- Preserved all API URLs/request/response/JSON and `LMS_###` error code values.

## Verification Run

- `./gradlew compileJava` passed with existing warnings.
- `./gradlew test --tests "*CourseServiceClassroomTest"` passed.

## Second Wave (post-merge, uncommitted as of 2026-07-14)

- Course: `CourseCompletionPolicy` (completeCourse validators), `CourseClassroomAssembler` (getCourseClassroom assembly, static).
- Quiz: `QuizAccessPolicy`, `QuizGrader`, `QuizInputValidator`.
- Diagnosis: `DiagnosisGrader`, `DiagnosisInputValidator`.
- Review: `ReviewRatingSummaryCalculator`.
- LearningProgress: classroom assembly de-duplicated (reuses `CourseClassroomAssembler`); `LearningProgressCalculator` (pure calc).
- Shared: `completion.application.service.CourseCompletionRegistrar` unifies completion creation for CourseService + QuizService.
- #1 Certificate: package corrected to `certificate.presentation.api`; `CertificateUseCase` added; controller injects the use case.
- #2 Chapter: command `MultipartFile -> UploadFile`; controller converts; `ChapterService` uses `CourseFileStoragePort` (behavior identical).
- All original code kept as reason-tagged comments; no dedicated tests for Quiz/Review/LearningProgress/Certificate/Chapter (compile-verified).

## Next Suggested Step

- Run full `./gradlew build` as a safety gate, then commit the second wave (scope only the changed files, avoid CRLF-only noise).
- Then a final cleanup slice: delete the commented-out relocated/dead code.
- #3/#4 (cross-domain ports/adapters) deferred by decision — over-engineering for one LMS module; revisit only if bounded contexts diverge.