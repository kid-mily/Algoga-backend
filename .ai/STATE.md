# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

## Current Work

- LMS package split is active on branch `refactor/completion-certificate-package`.
- Goal: split the old `lms` package into feature packages while keeping API URLs, request fields, response fields, JSON structure, and behavior unchanged.

## Progress

- Done in previous slices:
  - Course/Chapter core package split into `course`.
  - Country/Map package split into `country`.
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
  - Welcome coupon discount type changed from `RATE` to `PERCENT` for new signup coupons.

## Latest Verification

- `./gradlew clean compileJava` passed with existing warnings.
- `./gradlew build -x test` passed.

## Next Steps

1. Commit the Course Classroom/MyCourse package split slice.
2. Decide the next package slice:
   - Course file/storage remnants.
   - Course file/storage remnants.
   - CourseService orchestration extraction.
3. Keep each slice small and verify with `compileJava` and `build -x test`.

## Cautions

- Do not change API contracts unless explicitly requested.
- Avoid broad package declaration replacements; only edit moved files and required imports.
- Watch CRLF warnings due local Git config.
- Do not track unrelated local files.