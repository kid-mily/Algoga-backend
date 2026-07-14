# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

## Current Work

- CourseService responsibility-split refactor completed on branch `refactor/lms-cleanup-architecture` and merged into `develop` (2026-07-13).
- Goal: reduce CourseService size/coupling by extracting collaborators, and move Q&A ownership to the `qna` domain, while keeping API URLs, request fields, response fields, JSON structure, and error code values unchanged.
- Relocated/dead code is currently kept as comments (history preservation); real deletion is planned as a final cleanup slice.
- Second refactoring wave (post-merge, currently uncommitted): SRP decomposition of remaining large services + two convention fixes (#1 Certificate, #2 Chapter command). Same constraints (API/JSON/error codes unchanged; comment-do-not-delete).

## Progress

- Done in previous slices:
  - Course/Chapter core package split into `course`.
  - Country/Map repository/model package split into `country`.
  - Course application/presentation DTO package split into `course`.
  - Course cache settings split into `course.settings.cache`.
  - LearningProgress package split into `learningprogress`.
  - Enrollment package split into `enrollment`.
  - Quiz package split into `quiz`.
  - Completion package split into `completion`.
  - Certificate package split into `certificate`.
  - Review package split into `review`.
  - Q&A package split into `qna`.
  - Diagnosis package split into `diagnosis`.
  - Course statistics package split into `course.statistics`.
  - My-course/classroom/student presentation/result package split into course.
  - Map application/presentation package split into country.
  - Course file/storage remnants split into course.
  - Course usecase/command split into course.
  - Course UserProfile port/adapter split into course.
  - CourseService split into course.
  - LMS residual Java package cleanup into `learning`, `course`, and `docs/http/learning`.
  - Welcome coupon discount type changed from `RATE` to `PERCENT` for new signup coupons.
- Done in CourseService responsibility-split slice (2026-07-13):
  - File/thumbnail storage handling extracted to `CourseFileManager`.
  - Pure progress calculations (completed count, progress rate, total duration) extracted to `CourseProgressCalculator`.
  - Learning-progress DB/cache read and merge extracted to `CourseProgressReader`.
  - Admin course-student result assembly extracted to `CourseStudentResultAssembler`.
  - My-course result assembly extracted to `MyCourseResultAssembler`.
  - Q&A use case moved to the `qna` domain: added `qna.application.usecase.CourseQnaUseCase` and `qna.application.service.CourseQnaService`; repointed `CourseQnaController` and `AdminCourseQnaController` from `CourseUseCase` to `CourseQnaUseCase`; removed Q&A methods from `CourseUseCase`/`CourseService`. Dependency direction is now `qna -> course` (course no longer depends on qna).
  - Dead `LocalFileStorageManager` (superseded by S3) commented out.
  - All moved/removed code is commented out (not deleted) with reason comments; API URLs/request/response/JSON/error codes unchanged.
- Done in second wave (post-merge, uncommitted):
  - `completeCourse` completion validators extracted to `course.application.policy.CourseCompletionPolicy`.
  - `getCourseClassroom` assembly extracted to `course.application.service.CourseClassroomAssembler` (static; also reused by LearningProgressService).
  - Quiz: access/existence validation to `quiz.application.policy.QuizAccessPolicy`; submission grading to `quiz.application.service.QuizGrader`; input validation to `quiz.application.service.QuizInputValidator`.
  - Diagnosis: grading to `diagnosis.application.service.DiagnosisGrader`; input validation to `diagnosis.application.service.DiagnosisInputValidator`.
  - Review: rating summary to `review.application.service.ReviewRatingSummaryCalculator`.
  - LearningProgress: classroom assembly de-duplicated (reuses `CourseClassroomAssembler`); pure calc to `learningprogress.application.service.LearningProgressCalculator`.
  - Shared completion registration unified into `completion.application.service.CourseCompletionRegistrar` (used by both `CourseService.completeCourse` and `QuizService.submitQuiz`).
  - #1: `CertificateController` package corrected `completion.presentation.api` -> `certificate.presentation.api`; added `certificate.application.usecase.CertificateUseCase` (implemented by `CertificatePdfService`); controller injects the use case.
  - #2: `CreateChapterCommand`/`UpdateChapterCommand` `MultipartFile` -> `UploadFile`; `AdminChapterController` converts at the boundary; `ChapterService` switched from `global` `FileStoragePort` to `course` `CourseFileStoragePort` (identical S3 key/bucket/exception, so response unchanged).
  - CertificatePdfService (single-responsibility PDF generation) intentionally left as-is.
- Deferred by decision (2026-07-14): #3/#4 cross-domain decoupling via ports/adapters (CourseCompletionPolicy / QuizAccessPolicy still reference sibling-domain repositories directly). Judged over-engineering for a single LMS module sharing one datastore; revisit only if these become separate bounded contexts.

## Latest Verification

- Each second-wave slice verified with `./gradlew compileJava` (passed with existing warnings).
- `./gradlew test --tests "*CourseServiceClassroomTest"` and `./gradlew test --tests "*DiagnosisServiceTest"` passed. Other refactored services (Quiz/Review/LearningProgress/Certificate/Chapter) have no dedicated unit tests; verified by compile only.
- Pending before final commit: run a full `./gradlew build` once as the safety gate before deleting the commented-out code.

## Next Steps

1. Run full `./gradlew build` (safety gate), then commit the second-wave refactoring.
2. Final cleanup slice: delete the commented-out relocated/dead code once stabilized (was intentionally kept as comments through the whole effort).
3. Optionally add unit tests for the extracted pure utils (Grader/Calculator/Assembler) — cheap, high value.
4. (Deferred, optional) #3/#4 cross-domain port/adapter decoupling — only if bounded contexts diverge.
5. Deferred functional improvements (unchanged): restrict course material file types, preserve original PDF/material file name on download, review admin course/chapter/quiz registration flow, add classroom text length limits.

## Cautions

- Do not change API contracts unless explicitly requested.
- Preserve `LMS_###` error code values for frontend/API compatibility.
- Avoid broad formatting, import-order, or whitespace-only changes.
- Watch CRLF warnings due local Git config.
- Do not track unrelated local files.
- Relocated/dead code is intentionally kept as comments for now; do not delete until the final cleanup slice.