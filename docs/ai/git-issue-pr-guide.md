# Git, Issue, and PR Guide

Use this guide for branch names, issue titles, commit messages, and PR summaries.

## AI Rules

- Do not commit, push, create branches, or open PRs unless the user explicitly asks.
- Before commit or PR work, summarize the intended scope.
- Do not include secrets or private values in commit messages, issue bodies, or PR descriptions.

## Issue Titles

Use one of these prefixes:

- `[Feat]`
- `[Task]`
- `[Fix]`
- `[Ref]`
- `[Docs]`

Example:

```text
[Feat] LMS 강의 수강 진도 API 추가
```

## Branch Names

Recommended format:

```text
feature/short-task-name
fix/short-bug-name
refactor/short-scope
docs/short-topic
```

## Commit Messages

Use a conventional prefix:

- `feat:`
- `fix:`
- `refactor:`
- `test:`
- `docs:`
- `chore:`

Example:

```text
feat: add LMS progress enrollment validation
```

## PR Titles

Use one of these prefixes:

- `[Feat]`
- `[Update]`
- `[Fix]`
- `[Ref]`
- `[Docs]`

## PR Body Template

```md
## Summary

-

## Changes

-

## Frontend Impact

- API changed: yes/no
- Endpoint:
- Request change:
- Response change:
- ErrorCode change:
- Breaking change: yes/no
- Frontend action needed:

## Verification

-

## Notes

-
```

## Frontend Sharing Rule

Backend developers do not need to open PRs in the frontend repository unless the team explicitly asks.

For frontend-impacting backend changes:

- Update `.ai/API.md` only for the changed or risky API contract.
- Add the `Frontend Impact` section to the backend PR body.
- Share the backend PR link and Swagger URL with frontend developers.
- Treat Swagger/OpenAPI as the complete API reference.
- Treat `.ai/API.md` as change notes, not a full API catalog.
