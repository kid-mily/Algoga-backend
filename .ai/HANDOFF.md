# HANDOFF

## Current Goal

- Continue LMS package split without changing API contracts.

## Completed Recently

- Completion/certificate package split completed and committed.
- Review/Q&A package split completed and committed.
- Diagnosis package split completed and committed.
- Course statistics package split completed and committed.
- Welcome coupon type fix completed and committed.
- Course my-course/classroom/student result/response/controller package split completed and committed.
- Map/Country application/presentation package split completed and committed.
- Current working tree slice: Course file/storage package split.

## Current Slice Details

- Moved `LmsStorageSettings` to `course.settings.CourseStorageSettings`.
- Moved `CourseFileJpaEntity` to `course.infrastructure.persistence.entity`.
- Moved `LocalFileStorageManager` to `course.infrastructure.document`.
- Updated dependent imports in `ChapterService`, `CourseService`, `CourseMapper`, `CourseJpaEntity`, and `CourseServiceClassroomTest`.
- S3 bucket/directory strings remain unchanged (`algoga-lms`, `lms/...`) to preserve stored object paths.
- API URLs and JSON contracts are unchanged.

## Verification Run

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

## Next Suggested Slice

- Commit current slice first.
- Then choose one:
  - Split remaining course command/usecase/port files from `lms`.
  - Extract `CourseService` orchestration carefully after package moves are stable.
  - Decide what to do with shared `lms.exception` after remaining LMS services are moved.