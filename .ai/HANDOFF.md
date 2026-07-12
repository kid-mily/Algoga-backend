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
- Course usecase/command package split completed.
- Course UserProfile port/adapter package split completed.
- Current working tree slice: CourseService package split.

## Current Slice Details

- Moved `CourseService` to `course.application.service`.
- Updated `CourseServiceClassroomTest` to import the moved service.
- API URLs and JSON contracts are unchanged.
- Cache behavior and business logic are unchanged.

## Verification Run

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

## Next Suggested Slice

- Commit current slice first.
- Then prepare PR for accumulated LMS Course package split work.
- Remaining post-PR cleanup candidates: shared `lms.exception`, `CurrentUserIdResolver`, `LmsSchedulingConfig`, and leftover LMS test package names.