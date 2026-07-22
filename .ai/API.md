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

#### 2026-07-22

#### Endpoint

`POST /api/v1/admin/courses`

`PUT /api/v1/admin/courses/{courseId}`

`POST /api/v1/admin/courses/{courseId}/publish`

#### What Changed

- Initial course creation no longer publishes a course immediately. If `PUBLISHED` is sent on create, the course is stored as `DRAFT`.
- Added an admin publish API. Publishing validates basic course information, at least 1 chapter, and at least 1 quiz before changing status to `PUBLISHED`.
- Existing course update also runs the same chapter/quiz validation when `status` is changed to `PUBLISHED`.
- Course status now accepts `INCOMPLETE`, `DRAFT`, and `PUBLISHED`; user-facing course APIs and recommendation queries continue to expose only `PUBLISHED` courses.
- Lecture-only payment, bundle payment preview, bundle payment save, and lecture amount calculation now reject courses whose status is not `PUBLISHED`.

#### Request

```json
{}
```

#### Response

`POST /api/v1/admin/courses/{courseId}/publish`

```json
{
  "courseId": 1,
  "countryId": 1,
  "managerId": 2,
  "title": "string",
  "description": "string",
  "price": 100000,
  "maxRewardMileage": 1000,
  "thumbnailUrl": "https://cdn.example/thumb.png",
  "fileUrls": [],
  "files": [],
  "level": "BEGINNER",
  "levelName": "string",
  "status": "PUBLISHED",
  "deleted": false
}
```

#### Error Codes

- `COURSE_INCOMPLETE`: publish/update-to-published failed because the course is missing required basic information, chapter, or quiz.
- `PAY_017` / `COURSE_NOT_PUBLISHED`: payment attempted for a course that is not `PUBLISHED`.

#### Frontend Impact

- API changed: yes
- Endpoint: `POST /api/v1/admin/courses/{courseId}/publish`
- Request change: create/update status can include `INCOMPLETE`; create with `PUBLISHED` is saved as `DRAFT`.
- Response change: publish failure includes `data.hasChapter` and `data.hasQuiz` when chapter/quiz content is incomplete.
- ErrorCode change: yes, `COURSE_INCOMPLETE`, `COURSE_NOT_PUBLISHED`
- Breaking change: yes for any frontend that expected create-course with `PUBLISHED` to publish immediately.
- Frontend action needed: create courses as `DRAFT`, add chapters/quizzes, call publish API on the final step, and show missing publish requirements from `data`.

#### Notes

- Example publish failure body:

```json
{
  "errorCode": "COURSE_INCOMPLETE",
  "message": "챕터와 퀴즈가 각각 1개 이상 등록되어야 합니다.",
  "data": {
    "hasChapter": false,
    "hasQuiz": true
  }
}
```

#### 2026-07-14

#### Endpoint

`GET /api/v1/admin/stats/interest/lectures`

#### What Changed

- Added `averageProgressRate` to each lecture interest statistics item.
- Existing request parameters, endpoint path, and existing response fields remain unchanged.

#### Request

```json
{}
```

#### Response

```json
{
  "rank": 1,
  "lectureTitle": "도쿄 완전 정복 2024",
  "country": "일본",
  "enrollCount": 1842,
  "averageProgressRate": 72,
  "completionRate": 64.2
}
```

#### Error Codes

-

#### Frontend Impact

- API changed: yes
- Endpoint: `GET /api/v1/admin/stats/interest/lectures`
- Request change: no
- Response change: yes, `averageProgressRate` added
- ErrorCode change: no
- Breaking change: no
- Frontend action needed: use `averageProgressRate` for the average progress column in the statistics manager lecture table.

#### Notes

- CSV download at `GET /api/v1/admin/stats/interest/lectures/csv` now includes the average progress rate column.

#### 2026-07-16

#### Endpoint

`POST /api/v1/admin/courses/{courseId}/quizzes`

#### What Changed

- Added a server-side limit so each course can have at most 5 active quizzes.
- Existing request fields and response fields remain unchanged.

#### Request

```json
{
  "question": "string",
  "option1": "string",
  "option2": "string",
  "option3": "string",
  "option4": "string",
  "correctOption": 1,
  "explanation": "string"
}
```

#### Response

```json
{
  "quizId": 1,
  "courseId": 3,
  "question": "string",
  "option1": "string",
  "option2": "string",
  "option3": "string",
  "option4": "string",
  "correctOption": 1,
  "explanation": "string"
}
```

#### Error Codes

- `QUIZ_LIMIT_EXCEEDED` / `LMS_043`: a course already has 5 active quizzes.

#### Frontend Impact

- API changed: yes
- Endpoint: `POST /api/v1/admin/courses/{courseId}/quizzes`
- Request change: no
- Response change: no
- ErrorCode change: yes, `QUIZ_LIMIT_EXCEEDED`
- Breaking change: no
- Frontend action needed: prevent adding more than 5 quizzes per course when possible, and show the backend error message if the limit is exceeded.

#### Notes

- Deleted quizzes are not counted because the backend counts only `deleted = false` quizzes.

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
