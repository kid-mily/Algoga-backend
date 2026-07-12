# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- Do not record every teammate task here.

## Current Work

- LMS package split is active on branch `refactor/lms-package-split-next`.
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
- Verified latest Quiz slice with:
  - `./gradlew clean compileJava`
  - `./gradlew build -x test`

## Next Steps

1. Commit the Quiz package split slice.
2. Decide the next package slice:
   - `completion`/`certificate` if targeting course completion flow cleanup.
   - `review`/`qna` if targeting community interaction cleanup.
   - `diagnosis` if targeting assessment flow cleanup.
3. Keep each slice small and verify with `compileJava` and `build -x test`.

## Cautions

- Do not change API contract unless explicitly requested.
- Avoid broad package declaration replacements; only edit moved files and required imports.
- Watch CRLF warnings due local Git config.
- Do not track unrelated local files.