# STATE

## Scope

- This file tracks the current AI-assisted backend work in this repository.
- It is not a team-wide task board.
- Do not record every teammate's task here.

## Current Work

- LMS/Course clean architecture refactor is paused after several safe package-split commits.
- A short urgent stats support task was completed for the statistics manager.
- Next active work should resume the LMS/Course refactor after checking compile/test impact from latest `develop` merges.

## Current Branch

- `feature/course-clean-architecture-refactor`
- Local branch is synced with `origin/feature/course-clean-architecture-refactor` as of the last push.
- `docker-compose.yml` remains intentionally untracked.

## Progress

- Done:
  - Added AI collaboration documents and guides: `AGENTS.md`, `.ai/*`, `docs/ai/*`, `docs/design/figma-stats-manager-prompt.md`.
  - Split Course/Chapter domain models, repository ports, persistence entities, Spring Data repositories, adapters, mapper, S3 storage port/adapter, and course/chapter controllers into the `course` package.
  - Moved Chapter application layer into the `course` package.
  - Moved `CourseRepository` port into `course.domain.repository` and updated known main/test imports.
  - Added `CourseCompletionRepository.countByCourseIds(List<Long> courseIds)` for statistics manager lecture completion-rate support.
  - Updated booking main code import after latest `develop` merge introduced an old `CourseRepository` import.
  - Verified Java compilation with `./gradlew clean compileJava` before the latest booking import follow-up.
  - Pushed branch `feature/course-clean-architecture-refactor` to origin.
- In progress:
  - Keep branch aligned with latest `develop` while refactor is in progress.
  - Confirm remaining old package references before continuing the next split.
- Waiting:
  - PR review/merge status for `[Feat] 강의별 수료자 수 집계 메서드 추가`.

## Actual State Check

- `git status` showed only `docker-compose.yml` as untracked before updating these `.ai` files.
- Actual tracked docs are now committed in the branch, so `.ai` files can be edited and committed normally.
- Actual code still has at least one old test import detected:
  - `src/test/java/com/kidmily/algoga_server/booking/application/service/BookingCommandServiceTest.java`
  - `import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;`
- Main code old `CourseRepository` import was fixed in `BookingCommandService.java` and pushed.

## Next Steps

1. Run/search before new refactor work:
   - `rg "lms\.domain\.repository\.CourseRepository" src/main/java src/test/java`
2. Fix remaining old imports caused by the CourseRepository move, starting with `BookingCommandServiceTest` if still present.
3. Run verification:
   - `./gradlew clean compileJava`
   - Consider `./gradlew test` or at least test compilation if practical.
4. Resume LMS/Course refactor in small commits.
5. Next likely refactor candidate: inspect `CourseService` dependencies and split only a small, low-risk piece instead of moving the whole service at once.

## Out of Scope

- Do not change payment/webhook coupon behavior unless explicitly assigned; that was not the user's ownership area.
- Do not add `docker-compose.yml` unless the user explicitly decides to track it.
- Do not make broad line-ending, formatting, or import-order-only changes.

## Notes

- Existing Git global config has `core.autocrlf=true`, so watch for CRLF warnings and avoid broad staged diffs.
- Use file-specific `git add` when excluding `docker-compose.yml` or unrelated changes.
- Keep this file short and factual; use `.ai/WORKLOG.md` for completed work history.
