# AGENTS.md

This is the main AI working guide for the Algoga backend project.

Before changing code, classify the request and read only the documents needed for that task. Do not read every document every time.

## Core Rules

- Keep the existing architecture, package structure, naming style, and code style.
- Do not modify files outside the requested scope unless required to finish the task.
- Do not make broad formatting, import-order, or whitespace-only changes in existing files.
- If a policy is unclear and the choice has real impact, ask before deciding.
- After code changes, run the relevant verification command when possible.
- If the task changes current state, API behavior, or completed work, update the matching `.ai` document.
- Do not write secrets, tokens, passwords, private keys, or real credential values in code or documentation.

## Request Routing

### Backend Work

For Java, Spring Boot, JPA, Controller, Service, Repository, Entity, Mapper, ErrorCode, Swagger, validation, transaction, or hexagonal architecture work:

- Read `docs/ai/backend-convention.md`.
- If an API contract changes, also read and update `.ai/API.md`.
- If verification is needed, read `docs/ai/testing-guide.md`.

### API Contract Work

For endpoint, request, response, status code, error code, authentication, authorization, or frontend integration changes:

- Read `.ai/API.md`.
- Read `docs/ai/backend-convention.md`.
- Update `.ai/API.md` after the change.

### Domain or Naming Work

For domain terms, naming, API field names, frontend/backend wording, or unclear business vocabulary:

- Read `docs/ai/domain-glossary.md` if it exists.
- If the term is missing, ask the user or leave a note instead of inventing a business meaning.

### Continue Previous Work

If the user says "이어서", "어디까지 했어", "현재 상태 알려줘", "전에 하던 거 계속", or asks to resume:

- Read `.ai/STATE.md`.
- Read `.ai/HANDOFF.md`.
- Summarize the current state before continuing.

### Completion or Handoff

If the user says "완료", "끝났어", "기록해줘", "정리해줘", or asks to leave notes for the next chat:

- Update `.ai/WORKLOG.md`.
- Update `.ai/STATE.md` if the active work changed.
- Update `.ai/HANDOFF.md` if someone may continue later.
- Update `.ai/API.md` if the API changed.

### Testing and Build Work

For test failures, build failures, CI failures, or verification requests:

- Read `docs/ai/testing-guide.md`.
- Read the relevant backend convention only if code changes are needed.

### Git, Issue, Commit, or PR Work

For branch, issue, commit message, PR title/body, or review preparation:

- Read `docs/ai/git-issue-pr-guide.md`.
- Do not commit, push, or open a PR unless the user explicitly asks.

### Security Work

For authentication, authorization, token, environment variable, credential, file upload, privacy, or secret handling changes:

- Read `docs/ai/security-guide.md`.
- Read `.ai/API.md` if API behavior changes.

## Work Completion Checklist

Before finishing a code task:

1. Confirm the changed files match the requested scope.
2. Check that package direction and architecture rules are still valid.
3. Run the relevant test/build command if feasible.
4. Report what changed and what verification was run.
5. Mention any verification that could not be run.
