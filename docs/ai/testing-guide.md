# Testing Guide

Use this document to choose verification commands after backend changes.

## Backend Commands

Linux, macOS, or Git Bash:

```bash
./gradlew test
./gradlew build
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew test
.\gradlew build
.\gradlew bootRun
```

## Project Runtime

- Java version: `17`
- Spring Boot version: `3.5.14`
- Project name: `algoga_server`
- Backend server port: `15000`
- Frontend local dev port expected by backend config: `17000`
- Database driver: MySQL (`com.mysql:mysql-connector-j`)
- Local database name: `algoga`
- MySQL is required for tests that exercise CRUD or persistence behavior.
- Redis is used by the application.
- Redis may be started manually with Docker or through `docker-compose`, depending on the developer's local setup.
- `docker-compose.yml` currently defines Redis on `127.0.0.1:6379`.
- Local/Dev Swagger UI: `http://localhost:15000/swagger-ui/index.html#/`
- Production Swagger UI: `https://kidmily.kro.kr/swagger-ui/index.html#/`

## Local Requirements to Confirm

The following are inferred from configuration but should be confirmed by the team:

- Exact local MySQL startup method for each developer.
- Whether Redis is required for every test suite or only Redis-dependent features.
- Required `.env` keys for a full local run.
- Whether Ollama is needed for normal local development or only chatbot/vector features.

## When to Run What

- For small service, domain, mapper, or repository logic changes: run targeted tests if available, otherwise run `test`.
- For API, security, configuration, persistence, or cross-domain changes: run `test`, then `build` when feasible.
- For dependency, profile, or startup configuration changes: run `build`, then `bootRun` when feasible.
- If a command cannot be run, record why in the final response or `.ai/HANDOFF.md`.

## Test Writing Rules

- Prefer focused tests for changed behavior.
- Add broader tests when touching shared behavior, API contracts, persistence mapping, security, or error handling.
- Do not add superficial tests that only assert mocks were called unless that is the behavior under test.
- Keep test naming consistent with existing project style.

## Verification Record

When a task is completed, record important verification in `.ai/WORKLOG.md` if the user asks to keep history.

## Common Local Issues

### Redis Connection Failure

Check:

- Redis container is running.
- `REDIS_HOST` and `REDIS_PORT` are set.
- `docker-compose.yml` can start Redis locally.

### Database Connection Failure

Check:

- MySQL is running.
- `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are set.
- The configured database exists. Local database name is `algoga`.

### Missing Secret or Environment Variable

Check `docs/ai/security-guide.md` for required variable names. Do not write real secret values in documentation.
