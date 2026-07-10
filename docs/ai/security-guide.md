# Security Guide

Use this document for secrets, authentication, authorization, token handling, and privacy-sensitive changes.

## Secret Handling

- Do not write real passwords, tokens, secret keys, private keys, API keys, or credential values in code or documentation.
- Documentation may mention environment variable names and their purpose, but not real values.
- Do not commit `.env`, `application-secret.yml`, private key files, or local credential files.
- If a secret appears in logs, code, or docs, stop and ask how the team wants to rotate or remove it.

## Environment Variables

Document variables like this:

```text
VARIABLE_NAME: purpose and where it is used
```

Do not document actual values.

## Project Environment Variables

The application configuration references these variables. Document the purpose only; never write real values here.

### Database and Cache

- `DB_URL`: MySQL JDBC URL.
- `DB_USERNAME`: MySQL username.
- `DB_PASSWORD`: MySQL password.
- `REDIS_HOST`: Redis host.
- `REDIS_PORT`: Redis port.

### OAuth and Auth

- `GOOGLE_CLIENT_ID`: Google OAuth client id.
- `GOOGLE_CLIENT_SECRET`: Google OAuth client secret.
- `KAKAO_CLIENT_ID`: Kakao OAuth client id.
- `KAKAO_CLIENT_SECRET`: Kakao OAuth client secret.
- `JWT_SECRET_KEY`: JWT signing secret.
- `COOKIE_DOMAIN`: optional cookie domain. Empty/default supports localhost host-only cookies.

### Mail

- `GMAIL_USERNAME`: Gmail SMTP username.
- `GMAIL_PASSWORD`: Gmail SMTP password or app password.

### AI and External APIs

- `LLM_KEY`: Groq/OpenAI-compatible API key used by Spring AI config.
- `FLIGHT_BASE_URL`: flight API base URL.
- `FLIGHT_API_SERVICE_KEY`: flight API service key.

### Payment

- `PORTONE_BASE_URL`: PortOne API base URL.
- `PORTONE_STORE_ID`: PortOne store id.
- `PORTONE_API_SECRET`: PortOne API secret.
- `PORTONE_CHANNEL_KEY`: PortOne channel key.

### S3 or Object Storage

- `S3_ENDPOINT`: S3-compatible endpoint.
- `S3_PUBLIC_URL`: public URL for browser-accessible objects.
- `AWS_ACCESS_KEY`: object storage access key.
- `AWS_SECRET_KEY`: object storage secret key.

### Frontend

- `FRONTEND_URL`: frontend base URL. Local default is `http://localhost:17000`.

## Project Secret Files

Do not commit local secret files. This project uses `.env` for local environment values, and developers may also set values directly in IntelliJ Run Configuration.

Known project convention:

- `.env` is used.
- `application-secret.yml` is not used.
- IntelliJ Run Configuration may be used for local environment variables.

Keep these out of Git:

- `.env`
- private key files
- local credential files

## Authentication and Authorization

Before changing auth behavior:

1. Check existing Spring Security configuration.
2. Check JWT/OAuth2 behavior.
3. Check affected endpoints in `.ai/API.md`.
4. Document frontend impact if request headers, status codes, or error codes change.

## API Security

- Validate request bodies with `jakarta.validation` where appropriate.
- Keep authorization checks near the application or security boundary used by existing code.
- Avoid leaking internal exception details in API responses.
- Use existing global error handling patterns.

## File and Upload Security

- Validate file type, size, and storage path when file upload behavior changes.
- Do not trust original filenames for storage paths.
- Reuse existing S3 or storage utilities under `global` when available.
