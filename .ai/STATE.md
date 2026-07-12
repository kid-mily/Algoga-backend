# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

## Current Work

- LMS residual cleanup is active on branch `refactor/lms-cleanup-architecture`.
- Goal: remove the old Java `lms` package remnants while keeping API URLs, request fields, response fields, JSON structure, and error code values unchanged.

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

## Latest Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.
- `./gradlew testClasses` passed.

## Next Steps

1. Review and commit the LMS residual cleanup slice.
2. Decide whether to open a small PR for this cleanup before feature work.
3. After merge, start the next functional improvements:
   - restrict course material file types,
   - preserve original PDF/material file name on download,
   - review admin course/chapter/quiz registration flow,
   - add classroom text length limits.

## Cautions

- Do not change API contracts unless explicitly requested.
- Preserve `LMS_###` error code values for frontend/API compatibility.
- Avoid broad formatting, import-order, or whitespace-only changes.
- Watch CRLF warnings due local Git config.
- Do not track unrelated local files.