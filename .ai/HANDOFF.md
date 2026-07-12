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
- Course file/storage package split completed.
- Current working tree slice: Course usecase/command package split.

## Current Slice Details

- Moved `CourseUseCase` to `course.application.usecase`.
- Moved `CompleteCourseCommand` to `course.application.command`.
- Updated imports in course, completion, country, diagnosis, Q&A controllers/services, and diagnosis test.
- API URLs and JSON contracts are unchanged.

## Verification Run

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

## Next Suggested Slice

- Commit current slice first.
- Then choose one:
  - Move `CourseService` to `course.application.service` after checking imports and remaining dependencies.
  - Split `UserProfilePort`/`UserProfileAdapter` if moving `CourseService` exposes awkward LMS dependencies.
  - Decide what to do with shared `lms.exception` after remaining LMS services are moved.