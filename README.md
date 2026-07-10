# Algoga Backend

Algoga backend repository.

## AI Collaboration

AI와 함께 작업할 때는 아래 문서를 기준으로 합니다.

- AI 작업 진입점: `AGENTS.md`
- 현재 작업 상태: `.ai/STATE.md`
- 인수인계 메모: `.ai/HANDOFF.md`
- 작업 기록: `.ai/WORKLOG.md`
- API 변경 기록: `.ai/API.md`
- 백엔드 컨벤션: `docs/ai/backend-convention.md`
- 테스트/빌드 기준: `docs/ai/testing-guide.md`
- Git/PR 규칙: `docs/ai/git-issue-pr-guide.md`
- 보안 규칙: `docs/ai/security-guide.md`
- 도메인 용어집: `docs/ai/domain-glossary.md`
- 프론트 repo 전달 템플릿: `docs/ai/frontend-repo-template.md`

새 AI 채팅을 시작할 때는 이렇게 요청합니다.

```text
AGENTS.md를 먼저 읽고, 요청에 맞는 문서를 선택해서 확인한 뒤 작업해줘.
```

이어서 작업할 때는 이렇게 요청합니다.

```text
이어서 하자. .ai/STATE.md와 .ai/HANDOFF.md를 읽고 현재 상태부터 정리해줘.
```

## Runtime

- Java: `17`
- Backend local port: `15000`
- Frontend local port expected by backend: `17000`
- Local Swagger UI: `http://localhost:15000/swagger-ui/index.html`

## Common Commands

Windows PowerShell:

```powershell
.\gradlew test
.\gradlew build
.\gradlew bootRun
```

Linux, macOS, or Git Bash:

```bash
./gradlew test
./gradlew build
./gradlew bootRun
```
