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
- Current working tree slice: Course UserProfile port/adapter package split.

## Current Slice Details

- Moved LMS `UserProfilePort` to `course.application.port`.
- Moved LMS `UserProfileAdapter` to `course.infrastructure.user`.
- Updated imports in course, diagnosis, Q&A, review services/results, and related tests.
- Kept `@Component("lmsUserProfileAdapter")` unchanged to minimize hidden wiring impact.
- `benefit.application.port.UserProfilePort` remains separate and unchanged.
- API URLs and JSON contracts are unchanged.

## Verification Run

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

## Next Suggested Slice

- Commit current slice first.
- Then move `CourseService` to `course.application.service` if remaining dependencies look stable.
- After that, decide what to do with shared `lms.exception` and presentation support classes.