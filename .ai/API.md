# API

This file is not a full copy of every backend API.

Swagger/OpenAPI is the source of truth for the complete API list. Use this file only for API changes, frontend-impacting contracts, temporary decisions, and endpoints currently being developed.

## Project API Notes

- Backend server port: `15000`
- Frontend local dev port: `17000`
- Default frontend URL in backend config: `http://localhost:17000`
- Main API prefix used by most controllers: `/api/v1`
- Legacy/example endpoint prefix may exist, such as `/api/examples`.

## Swagger

- Local/Dev Swagger URL: `http://localhost:15000/swagger-ui/index.html#/`
- Production Swagger URL: `https://kidmily.kro.kr/swagger-ui/index.html#/`

## What to Record Here

Record an entry when any of these change:

- endpoint path or HTTP method
- request body, query parameter, path variable, or multipart field
- response shape or field meaning
- status code
- error code
- authentication or authorization behavior
- frontend behavior or screen integration
- breaking changes

Do not record all existing endpoints just because they exist.

## Recent API Changes

### Template

#### Date

`YYYY-MM-DD`

#### Endpoint

`METHOD /api/v1/...`

#### What Changed

-

#### Request

```json
{}
```

#### Response

```json
{}
```

#### Error Codes

-

#### Frontend Impact

-

#### Notes

-

## Frontend Sharing Rule

For backend PRs, summarize frontend-impacting API changes in the PR body. Use this format:

```md
## Frontend Impact

- API changed: yes/no
- Endpoint:
- Request change:
- Response change:
- ErrorCode change:
- Breaking change: yes/no
- Frontend action needed:
- API notes: `.ai/API.md`
```
