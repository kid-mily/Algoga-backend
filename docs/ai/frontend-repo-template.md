# Frontend Repository AI Document Template

This backend developer does not need to open PRs directly in the frontend repository. Share this template with the frontend team if they want the same AI collaboration structure.

Note: `C:\Algoga-Frontend` may exist locally as a personal test checkout, but it is not the actual frontend team's working repository unless the team confirms it.

## Recommended Frontend Structure

```text
frontend-repo/
  AGENTS.md
  CLAUDE.md
  README.md

  .ai/
    STATE.md
    WORKLOG.md
    API.md
    HANDOFF.md

  docs/
    ai/
      frontend-convention.md
      testing-guide.md
      git-issue-pr-guide.md
      security-guide.md
```

## Frontend Tech Stack

- Framework: React + Next.js
- Language: TypeScript
- Package manager: npm
- Global state management: no separate global state library
- API calls: custom fetch-based API client
- Styling: Tailwind CSS
- Local dev server port: `17000`
- Dev command: `npm run dev`
- Build command: `npm run build`
- Lint command: `npm run lint`
- `npm run dev` runs `next dev -p 17000`

## Suggested Frontend AGENTS.md

```md
# AGENTS.md

## Core Rules

- Classify the request first and read only the needed documents.
- Follow the existing folder structure, component style, naming, and Tailwind conventions.
- For API integration work, read `.ai/API.md` first.
- If backend behavior is unclear, check Swagger or ask the backend owner.
- After changes, run lint/build/test when feasible.

## Frontend Work

For pages, components, hooks, styles, routing, or UI state:

- Read `docs/ai/frontend-convention.md`.

## API Integration Work

For custom API client, fetch calls, query/mutation behavior, error handling, auth token handling, or response mapping:

- Read `.ai/API.md`.
- Read `docs/ai/frontend-convention.md`.

## Continue Previous Work

If the user says "이어서", "어디까지 했어", or "현재 상태 알려줘":

- Read `.ai/STATE.md`.
- Read `.ai/HANDOFF.md`.

## Completion

When work is done:

- Update `.ai/WORKLOG.md` if the user wants a record.
- Update `.ai/API.md` if API integration changed.
- Update `.ai/STATE.md` if the current state changed.
```

## Suggested Frontend Convention

```md
# Frontend Convention

## Tech Stack

- React + Next.js
- TypeScript
- npm
- Tailwind CSS
- Custom fetch-based API client
- No separate global state management library

## Commands

```bash
npm run dev
npm run build
npm run lint
```

Local dev server uses port `17000`.

## API Rules

- Use the existing custom fetch-based API client.
- Do not create a second API client without team agreement.
- Keep request/response types close to the API module or feature convention used in the repo.
- Handle auth/token behavior through the existing client.
- Use backend Swagger for the full API reference.
- Use backend `.ai/API.md` or PR `Frontend Impact` sections for recent changes.

## Styling Rules

- Use Tailwind CSS.
- Follow existing spacing, color, and component patterns.
- Avoid introducing a new styling library without team agreement.
```

## Suggested Frontend API.md

```md
# API

## Backend Source

- Backend Swagger:
- Backend repo:
- Backend API change notes: backend `.ai/API.md`

## Used Endpoints

### METHOD /api/v1/...

#### Used In

-

#### Request Fields

-

#### Response Fields Used

-

#### Error Handling

-

#### Notes

-
```
