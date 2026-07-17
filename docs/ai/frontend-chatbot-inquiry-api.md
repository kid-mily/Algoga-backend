# 챗봇 & 1:1 문의 — 프론트엔드 연동 명세

> AI/프론트가 이 문서만 보고 바로 구현할 수 있도록 작성한 API 계약서.
> 대상 도메인: **챗봇(Chatbot)**, **1:1 문의(Inquiry)**, 그리고 문의 답변 알림에 쓰이는 **알림(Notification)** 일부.
> 사용자 파트 / 관리자 파트로 나누어 정리한다.

---

## 0. 공통 규약 (모든 API 공통)

- **Base URL**: `/api/v1`
- **Content-Type**: `application/json`
- **날짜 형식**: ISO-8601 UTC (`2026-07-17T02:56:48Z`)

### 0-1. 인증
| 구분 | 요구사항 |
|---|---|
| 사용자 API | 로그인 필수. JWT를 `Authorization` 헤더 또는 인증 쿠키로 전송. 없으면 `401` |
| 관리자 API | `CS_MANAGER` 또는 `SUPER_ADMIN` 권한 필요. 부족하면 `403` |

### 0-2. 성공 응답 봉투 `ApiResponse<T>`
모든 성공 응답은 이 형태로 감싸진다. **실제 데이터는 항상 `data` 안에 있다.**
```jsonc
{
  "timestamp": "2026-07-17T02:56:48Z",
  "status": 200,
  "code": "CHATBOT_ANSWERED",   // 비즈니스 코드 (성공 케이스 식별용)
  "message": "챗봇 답변 완료",
  "data": { /* 실제 페이로드 */ }
}
```
> 일부 커맨드성 API(읽음 처리/삭제 등)는 `data` 없이 `204 No Content`를 반환한다. 각 엔드포인트에 명시.

### 0-3. 에러 응답 봉투 `ErrorResponse`
```jsonc
{
  "timestamp": "2026-07-17T02:56:48Z",
  "status": 400,
  "errorCode": "INQ_002",       // 도메인별 에러 코드
  "message": "문의 제목이 유효하지 않거나 너무 짧습니다.",
  "traceId": "6c98f70b"         // 서버 로그 추적용 (버그 리포트 시 첨부)
}
```

### 0-4. 페이지 응답 봉투 `PageResponse<T>`
```jsonc
{
  "content": [ /* T[] */ ],
  "page": 0,
  "size": 20,
  "totalElements": 35,
  "totalPages": 2,
  "first": true,
  "last": false
}
```
> ⚠️ 직렬화 시 `@class` 필드가 함께 붙는다(Jackson 타입 정보). 프론트는 무시.

---

## 1. Enum 레퍼런스 (프론트 상수화용)

```ts
// 문의 카테고리 — 서버 전송은 code, 화면 노출은 description
type InquiryCategory = "RESERVATION" | "REFUND" | "COURSE" | "ETC";
const INQUIRY_CATEGORY_LABEL = {
  RESERVATION: "예약",
  REFUND: "환불",
  COURSE: "강의",
  ETC: "기타",
} as const;

// 문의 답변 상태
type InquiryStatus = "PENDING" | "ANSWERED";   // 답변대기 | 답변완료

// 챗봇 응답 모드 — 프론트 UI 분기의 핵심
type ChatbotMode = "NORMAL" | "REJECTED" | "AGENT_HANDOFF" | "RATE_LIMITED";

// 통합 히스토리 항목 타입
type HistoryType = "CHATBOT" | "INQUIRY";

// 통합 히스토리 항목 상태
//  - CHATBOT: COMPLETED(정상) | FILTERED(도메인 외 차단)
//  - INQUIRY: PENDING(답변대기) | ANSWERED(답변완료)
type HistoryStatus = "COMPLETED" | "FILTERED" | "PENDING" | "ANSWERED";

// 알림 타입 (문의 답변 관련만 발췌 — 전체는 알림 도메인 참고)
type NotificationType = "INQUIRY_ANSWERED" /* | ... */;
```

---

# 🟦 사용자 파트 (USER)

## 2. 챗봇 — `/api/v1/chatbot` (로그인 필수)

### 2-1. 통합 채팅 내역 조회
`GET /api/v1/chatbot/history?page=0&size=20`

- 챗봇 대화 + 1:1 문의가 **시간순으로 통합**되어 내려온다. `page=0`이 가장 최신.
- `data`: `PageResponse<UnifiedChatHistoryItem>`

**UnifiedChatHistoryItem**
```jsonc
{
  "id": "INQ_12",                 // "CHAT_{id}" 또는 "INQ_{id}"
  "type": "INQUIRY",              // CHATBOT | INQUIRY
  "question": "질문 또는 문의 내용",
  "answer": "답변 (대기 상태면 null)",
  "status": "ANSWERED",           // COMPLETED | FILTERED | PENDING | ANSWERED
  "createdAt": "2026-07-17T02:56:48Z",
  "isAnswerUnread": true          // ⭐ 답변이 등록됐지만 사용자가 아직 확인 안 함 → "답변 완료" 뱃지
}
```
> `isAnswerUnread`는 `type=INQUIRY`이고 답변 알림이 미읽음일 때만 `true`. 챗봇 대화(`type=CHATBOT`)는 항상 `false`.

### 2-2. 예상 질문 버튼 목록
`GET /api/v1/chatbot/suggested-questions`

- `data`: `Array<{ suggestedQuestionId: number, question: string }>`
- 버튼에 `question` 텍스트를 노출.

### 2-3. 예상 질문 버튼 클릭 → 답변
`POST /api/v1/chatbot/suggested-questions/ask`
```json
{ "suggestedQuestionId": 1 }
```
- `data`: `ChatbotAnswerResponse` (아래 2-5). 저장된 답변이라 항상 `mode: "NORMAL"`.
- 없는 ID → `404` `CHAT_005`

### 2-4. 챗봇 직접 질문 ⭐ (레이트리밋 적용)
`POST /api/v1/chatbot/ask`
```json
{ "question": "결제 내역은 어디서 확인하나요?" }
```
- `question`: 필수, 최대 1000자
- `data`: `ChatbotAnswerResponse`

### 2-5. `ChatbotAnswerResponse` — UI 분기의 핵심 ⭐⭐
```jsonc
{
  "answer": "챗봇 답변 텍스트",
  "isSuccess": true,
  "mode": "NORMAL",            // ← 이 값으로 분기
  "handoffSummary": null,      // AGENT_HANDOFF 일 때만 채워짐
  "handoffInquiry": null       // AGENT_HANDOFF 일 때만 채워짐
}
```

| `mode` | 의미 | 프론트 처리 |
|---|---|---|
| `NORMAL` | 정상 답변 | `answer`를 말풍선으로 표시 |
| `REJECTED` | 도메인 외 질문 차단 | `answer`(안내문) 표시. `isSuccess=false` |
| `AGENT_HANDOFF` | 상담원 연결 전환 | 입력 UI를 상담원 모드로 전환 + **문의 등록 폼으로 이동/프리필** (4-1 흐름 참고) |
| `RATE_LIMITED` | 요청 제한 | `answer`(대기 안내문) 표시 |

> ⚠️ **HTTP 상태로 성공/실패를 판단하지 말 것.** `NORMAL`/`REJECTED`/`AGENT_HANDOFF`는 모두 `200`으로 온다. 반드시 `data.mode` / `data.isSuccess`로 분기.

### 2-6. 레이트리밋 동작
- 제한: **2초당 1회(버스트)** + **1시간당 30회(지속)**
- 초과 시:
  - HTTP `429`, 응답 헤더 `Retry-After: <초>`
  - body는 여전히 `ApiResponse` 봉투, `code: "CHATBOT_RATE_LIMITED"`, `data.mode: "RATE_LIMITED"`
- 별개로 **일일 상담 횟수 초과**는 에러 응답: `429` `CHAT_001`

---

## 3. 1:1 문의 — `/api/v1/inquiries` (로그인 필수)

### 3-1. 문의 카테고리 목록
`GET /api/v1/inquiries/categories`
- `data`: `Array<{ code: InquiryCategory, description: string }>`
- 셀렉트 박스: **서버 전송은 `code`, 화면 표시는 `description`.**

### 3-2. 문의 등록 ⭐
`POST /api/v1/inquiries`
```json
{
  "category": "REFUND",
  "title": "환불 절차 문의드립니다.",
  "content": "어제 결제했는데 환불 규정이 어떻게 되나요?"
}
```
**검증 규칙 (프론트에서 선검증 권장):**

| 필드 | 규칙 | 위반 시 |
|---|---|---|
| `category` | 필수, `InquiryCategory` enum 값만 | `400` (다른 값이면 역직렬화 실패) |
| `title` | 필수, **2자 이상** ~ 100자 이하 | `400` `INQ_002` |
| `content` | 필수, **5자 이상** ~ 2000자 이하 | `400` `INQ_003` |

- 성공: `200`, `code: "INQUIRY_CREATED"`, `data: null`

> 등록한 문의는 별도 "문의함"이 아니라 **2-1 통합 히스토리**에 `type=INQUIRY`로 함께 조회된다.

### 3-3. 문의 답변 확인 처리 (뱃지 해제) ⭐
`PATCH /api/v1/inquiries/{id}/answer/read`
- 챗봇 창에서 사용자가 해당 문의의 답변을 열어봤을 때 호출한다. 이 문의의 "답변 완료" 뱃지를 해제한다.
- `{id}`는 통합 히스토리 항목 `id`("INQ_12")에서 `INQ_` 제거해 추출.
- 성공: `200`, `code: "INQUIRY_ANSWER_READ"`, `data: null`
- 본인 문의가 아니거나 없는 문의 → `404` `INQUIRY_NOT_FOUND`
> 이 뱃지 상태는 종(bell) 알림과 **독립적**이다. 챗봇에서 확인해도 종 알림 unread-count에는 영향 없음(반대도 마찬가지).

---

## 4. 사용자 주요 흐름

### 4-1. 상담원 연결(AGENT_HANDOFF) → 문의 폼 프리필
`POST /chatbot/ask` 응답의 `mode === "AGENT_HANDOFF"`이면:
1. 챗봇 입력 UI → 상담원 연결/문의 작성 모드로 전환
2. 문의 등록 폼(3-2)을 프리필:
   - 내용(`content`) ← `handoffInquiry` (사용자가 입력한 원본 문의)
   - 참고 요약 ← `handoffSummary` (AI가 만든 대화 요약, 상담원 맥락용)
3. 사용자가 카테고리/제목 확인 후 문의 등록 API 호출

### 4-2. 챗봇 "답변 완료" 뱃지 + 해제 ⭐
1. 챗봇 열 때 `GET /chatbot/history` 조회 → `isAnswerUnread === true`인 문의 항목에 **"답변 완료" 뱃지** 표시
2. 사용자가 그 문의의 답변을 열어보면, 뱃지 해제를 위해 아래 호출:
   `PATCH /api/v1/inquiries/{inquiryId}/answer/read` → `200`
   - `inquiryId`는 히스토리 `id`("INQ_12")에서 `INQ_` 제거해 추출
3. 다음 히스토리 조회부터 해당 항목 `isAnswerUnread === false`

```ts
// 뱃지 렌더
history.content
  .filter(m => m.type === "INQUIRY" && m.isAnswerUnread)
  .forEach(m => showBadge(m.id));

// 답변 확인 시 뱃지 해제
const inquiryId = m.id.replace("INQ_", "");
await patch(`/api/v1/inquiries/${inquiryId}/answer/read`);
```
> 이 뱃지 상태는 문의 도메인이 자체적으로 관리하며, 종(bell) 알림과 **독립적**이다(서로 영향 없음).

### 4-3. (선택) 알림 뱃지 연동
문의 답변 시 `INQUIRY_ANSWERED` 알림이 자동 생성된다. 종 아이콘 뱃지로도 알릴 수 있다.
- `GET /api/v1/notifications/unread-count` → `data: { count: number }`
- `GET /api/v1/notifications?isRead=false&page=1&size=8` → 목록 (아래 6장)
> 단, 문의 알림은 필수 알림이 아니라서, 사용자가 알림 설정에서 문의 알림을 끄면 생성되지 않는다.

---

# 🟥 관리자 파트 (ADMIN)

> 모든 관리자 API는 `CS_MANAGER` 또는 `SUPER_ADMIN` 권한 필요. 부족 시 `403`.

## 5. 챗봇 지식 관리 — `/api/v1/admin/chatbot`

사용자 화면(2-2)에 노출되는 **예상 질문 버튼**의 CRUD.

| 메서드 | 경로 | 요청 body | 응답 `data` |
|---|---|---|---|
| `POST` | `/suggested-questions` | `{ question, answer }` (둘 다 필수) | `null` |
| `GET` | `/suggested-questions` | — | `Array<{ suggestedQuestionId, question, answer }>` |
| `GET` | `/suggested-questions/{id}` | — | `{ suggestedQuestionId, question, answer }` |
| `PUT` | `/suggested-questions/{id}` | `{ question, answer }` | `null` |
| `DELETE` | `/suggested-questions/{id}` | — | `null` |

- 없는 ID → `404` `CHAT_005`
- 관리자 조회는 사용자용과 달리 `answer`까지 함께 내려온다.

## 6. 1:1 문의 관리 — `/api/v1/admin/inquiries`

### 6-1. 문의 목록 (페이징 + 교차 필터)
`GET /api/v1/admin/inquiries?category=ALL&status=ALL&page=0`

- **페이지당 8개 고정**
- `category`: `RESERVATION`/`REFUND`/`COURSE`/`ETC`, 전체는 생략 또는 `ALL`
- `status`: `PENDING`/`ANSWERED`, 전체는 생략 또는 `ALL`
- 잘못된 값은 서버가 "전체"로 처리(에러 아님)
- `data`: `PageResponse<InquiryAdminResponse>`

**InquiryAdminResponse**
```jsonc
{
  "inquiryId": 12,
  "userId": 5,
  "category": "REFUND",
  "title": "환불 문의",
  "content": "본문...",
  "answer": "답변 (미답변이면 null)",
  "status": "PENDING",            // PENDING | ANSWERED
  "createdAt": "2026-07-17T02:56:48Z",
  "answeredAt": null              // 미답변이면 null
}
```

### 6-2. 문의 답변 등록
`PUT /api/v1/admin/inquiries/{id}/answer`
```json
{ "answer": "안녕하세요, 환불 규정은 ... 입니다." }
```
- `answer`: 필수(공백 불가)
- 성공: `code: "INQUIRY_ANSWERED"`, `data: null`. 상태가 `ANSWERED`로 전환되고, 작성자에게 `INQUIRY_ANSWERED` 알림 자동 발송.
- 없는 문의 → `404` `INQUIRY_NOT_FOUND`
- 이미 답변됨 → `400` `ALREADY_ANSWERED_INQUIRY` (재답변 불가)

---

## 7. 알림 API (문의 답변 뱃지 연동용) — `/api/v1/notifications` (로그인 필수)

문의 답변 뱃지/알림에 필요한 것만 정리. (전체 알림 도메인의 일부)

| 메서드 | 경로 | 설명 | 응답 |
|---|---|---|---|
| `GET` | `/unread-count` | 안 읽은 알림 개수(종 뱃지) | `data: { count: number }` |
| `GET` | `/notifications?page=1&size=8&isRead=false` | 알림 목록(최신순). `isRead` 생략=전체 | `data: NotificationListResponse` |
| `PATCH` | `/{notificationId}/read` | 개별 읽음 | `204` |
| `PATCH` | `/read-all` | 전체 읽음 | `204` |
| `DELETE` | `/{notificationId}` | 개별 삭제 | `204` |
| `DELETE` | `/` | 전체 삭제 | `204` |

> ⚠️ 알림 API의 페이지 번호는 **1부터 시작**(챗봇 히스토리는 0부터). 혼동 주의.

**NotificationListResponse**
```jsonc
{
  "unreadCount": 2,
  "hasUnread": true,
  "notifications": [
    {
      "notificationId": 1,
      "type": "INQUIRY_ANSWERED",
      "message": "문의하신 내용에 답변이 등록되었습니다",
      "detail": "답변 내용...",
      "referenceId": 12,          // 문의답변이면 inquiryId
      "isRead": false,
      "createdAt": "2026-07-17T02:56:48Z"
    }
  ],
  "hasNext": true,
  "totalElements": 8,
  "totalPages": 3,
  "currentPage": 1
}
```

---

## 8. 에러 코드 표

| errorCode | HTTP | 발생 상황 |
|---|---|---|
| `CHAT_001` | 429 | 일일 챗봇 상담 횟수 초과 |
| `CHAT_002` | 500 | LLM 서버 연결 오류 |
| `CHAT_003` | 500 | 벡터 검색 오류 |
| `CHAT_005` | 404 | 예상 질문 없음 |
| `INQ_002` | 400 | 문의 제목 유효하지 않음/너무 짧음(2자 미만) |
| `INQ_003` | 400 | 문의 내용 유효하지 않음/너무 짧음(5자 미만) |
| `INQ_004` | 400 | 이미 답변 완료된 문의 (재답변 불가) |
| `INQ_001` (`INQUIRY_NOT_FOUND`) | 404 | 문의 없음 |
| `NOTIFICATION_NOT_FOUND` | 404 | 알림 없음 |
| `NOTIFICATION_UNAUTHORIZED` | 403 | 남의 알림 접근 |
| 공통 validation | 400 | `@Valid` 실패. `message`에 `필드: 사유` 형식 |

---

## 9. 구현 체크리스트

**사용자**
- [ ] 모든 응답에서 `data` 언래핑
- [ ] 챗봇 성공/실패는 `data.mode`/`data.isSuccess`로 분기 (HTTP 코드 아님)
- [ ] `AGENT_HANDOFF` → 상담원 모드 전환 + 문의 폼에 `handoffInquiry`/`handoffSummary` 프리필
- [ ] 문의 폼: `code` 전송 / `description` 표시, 제목 2자·내용 5자 최소 검증
- [ ] 챗봇 히스토리 `isAnswerUnread`로 "답변 완료" 뱃지 렌더, 답변 확인 시 `PATCH /inquiries/{id}/answer/read` 호출
- [ ] `429` 수신 시 `Retry-After`만큼 입력 잠금 + 안내

**관리자**
- [ ] 문의 목록: 페이지당 8개, `category`/`status` 교차 필터(`ALL` 지원)
- [ ] 답변 등록 후 목록 갱신, `ALREADY_ANSWERED_INQUIRY` 방어(이미 답변된 건 버튼 비활성)
- [ ] 예상 질문 버튼 CRUD

**공통**
- [ ] 알림 페이지 번호는 1-base, 챗봇 히스토리는 0-base
- [ ] 에러 시 `traceId`를 로깅/리포트에 포함
