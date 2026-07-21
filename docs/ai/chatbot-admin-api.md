# 챗봇 관리자 API 연동 가이드 (프론트엔드용)

관리자 페이지에 추가된 챗봇 API의 프론트 연동 문서입니다.

- **① 챗봇 대화 로그 조회** — 모든 유저의 챗봇 질문/답변 로그를 필터·검색·페이징으로 조회
- **② 규정 문서/페이지별 RAG 채택 빈도** — 챗봇이 답변 근거로 채택한 규정 문서·페이지의 사용 빈도 집계 (페이징)
- **③ CSV 다운로드** — ①·② 데이터를 필터 조건 그대로 CSV 파일로 내려받기

> 권한: 두 API 모두 **관리자 전용** (`CS_MANAGER` / `SUPER_ADMIN`). 요청에 관리자 인증(쿠키/토큰)이 필요합니다.

---

## 공통 응답 래퍼

모든 응답은 아래 형태로 감싸집니다. 실제 데이터는 `data`에 들어 있습니다.

```json
{
  "timestamp": "2026-07-20T14:30:00Z",
  "status": 200,
  "code": "문자열 코드",
  "message": "설명 메시지",
  "data": { }
}
```

날짜/시각은 모두 ISO-8601 문자열입니다. 서버는 KST 기준으로 동작합니다.

---

## ① 챗봇 대화 로그 조회

```
GET /api/v1/admin/chatbot/chat-logs
```

모든 유저의 챗봇 대화 로그를 **최신순**으로 페이징 조회합니다. (페이지당 **20건** 고정)

### 요청 쿼리 파라미터 (모두 선택)

| 이름 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `filtered` | boolean | (전체) | `true`=차단된 질문만, `false`=정상 질문만, 미지정=전체 |
| `from` | string `yyyy-MM-dd` | (제한 없음) | 조회 시작일(당일 포함) |
| `to` | string `yyyy-MM-dd` | (제한 없음) | 조회 종료일(당일 끝까지 포함) |
| `keyword` | string | (없음) | 질문 또는 답변 본문에 포함된 검색어 |
| `page` | number | `0` | 페이지 번호 (0부터 시작) |

### 응답 `data` (페이지 객체)

| 필드 | 타입 | 설명 |
|---|---|---|
| `content` | array | 로그 항목 배열 (아래 표) |
| `page` | number | 현재 페이지 번호 |
| `size` | number | 페이지 크기 (20) |
| `totalElements` | number | 전체 로그 수 |
| `totalPages` | number | 전체 페이지 수 |
| `first` | boolean | 첫 페이지 여부 |
| `last` | boolean | 마지막 페이지 여부 |

#### `content[]` 항목

| 필드 | 타입 | 설명 |
|---|---|---|
| `chatLogId` | number | 로그 ID |
| `userId` | number | 작성자 회원 PK |
| `userName` | string \| null | 작성자 이름 (탈퇴/삭제 시 null) |
| `userNickname` | string \| null | 작성자 닉네임 (미설정/탈퇴 시 null) |
| `question` | string | 사용자 질문 |
| `answer` | string | 챗봇 답변 (`filtered=true`면 거절 메시지) |
| `filtered` | boolean | `true`=도메인 외/차단된 질문 |
| `createdAt` | string(ISO) | 발생 시각 |

### 응답 예시

```json
{
  "timestamp": "2026-07-20T14:30:00Z",
  "status": 200,
  "code": "ADMIN_CHAT_LOGS_LOADED",
  "message": "챗봇 대화 로그 조회 성공",
  "data": {
    "content": [
      {
        "chatLogId": 1201,
        "userId": 1001,
        "userName": "김진도",
        "userNickname": "jindo",
        "question": "환불 언제까지 가능해요?",
        "answer": "환불은 결제일로부터 7일 이내에 가능합니다...",
        "filtered": false,
        "createdAt": "2026-07-20T14:29:00Z"
      },
      {
        "chatLogId": 1200,
        "userId": 1050,
        "userName": "이영희",
        "userNickname": null,
        "question": "오늘 서울 날씨 어때?",
        "answer": "죄송합니다. 저는 알고가 서비스와 관련된 질문에만 답변해 드릴 수 있어요.",
        "filtered": true,
        "createdAt": "2026-07-20T14:10:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 342,
    "totalPages": 18,
    "first": true,
    "last": false
  }
}
```

> ℹ️ `data`에 `@class` 라는 필드가 함께 내려올 수 있습니다(직렬화 메타). **무시하세요.**

### 요청 예시

```
GET /api/v1/admin/chatbot/chat-logs?filtered=true&from=2026-07-01&to=2026-07-20&keyword=환불&page=0
```

---

## ② 규정 문서/페이지별 RAG 채택 빈도

```
GET /api/v1/admin/chatbot/rag-source-stats
```

챗봇이 답변 **근거로 채택한**(검색되어 실제 사용된) 규정 문서·페이지의 빈도를 **`count` 내림차순**으로 **페이징**(페이지당 20건) 집계합니다.

### 요청 쿼리 파라미터 (모두 선택)

| 이름 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `from` | string `yyyy-MM-dd` | 전체 기간 | 집계 시작일(당일 포함) |
| `to` | string `yyyy-MM-dd` | 전체 기간 | 집계 종료일(당일 끝까지 포함) |
| `page` | number | `0` | 페이지 번호(0부터) |

### 응답 `data` (페이지 객체)

`content[]` 항목:

| 필드 | 타입 | 설명 |
|---|---|---|
| `source` | string | 규정 문서명 |
| `page` | number \| null | 페이지(1-based). 페이지 정보 없으면 null |
| `count` | number | 채택 횟수 |

나머지 페이지 필드(`page`, `size`, `totalElements`, `totalPages`, `first`, `last`)는 ①과 동일.

### 응답 예시

```json
{
  "timestamp": "2026-07-20T14:30:00Z",
  "status": 200,
  "code": "ADMIN_RAG_SOURCE_STATS_LOADED",
  "message": "RAG 채택 빈도 조회 성공",
  "data": {
    "content": [
      { "source": "환불규정.pdf", "page": 3, "count": 42 },
      { "source": "수료기준.pdf", "page": 1, "count": 27 },
      { "source": "쿠폰정책.pdf", "page": null, "count": 8 }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 37,
    "totalPages": 2,
    "first": true,
    "last": false
  }
}
```

### 요청 예시

```
GET /api/v1/admin/chatbot/rag-source-stats?from=2026-07-01&to=2026-07-20&page=0
```

---

## ③ CSV 다운로드

①·② 데이터를 **조회와 동일한 필터**로 CSV 파일로 내려받습니다. **페이징 없이 조건에 맞는 전체**를 내보냅니다. 응답은 JSON 래퍼가 아니라 **CSV 파일**(`text/csv; charset=UTF-8`, UTF-8 BOM 포함 → 엑셀 한글 정상)이며, `Content-Disposition: attachment` 로 다운로드됩니다.

### 대화 로그 CSV
```
GET /api/v1/admin/chatbot/chat-logs/export?filtered=&from=&to=&keyword=
```
- 파라미터: `filtered`, `from`, `to`, `keyword` (①과 동일, `page` 없음)
- 파일명: `chatbot-chat-logs.csv`
- 컬럼: `chatLogId, userId, userName, userNickname, question, answer, filtered, createdAt`

### RAG 채택 빈도 CSV
```
GET /api/v1/admin/chatbot/rag-source-stats/export?from=&to=
```
- 파라미터: `from`, `to` (②와 동일, `page` 없음)
- 파일명: `chatbot-rag-source-stats.csv`
- 컬럼: `source, page, count`

### 프론트 다운로드 구현 팁
- `<a href>` 로 바로 열거나, fetch로 받아 `Blob` → `URL.createObjectURL` → 임시 `<a download>` 클릭 방식.
- 인증(쿠키/토큰)이 필요하므로, 단순 링크로 안 되면 fetch에 인증 헤더 실어 Blob으로 받는 방식을 쓰세요.

### 유의사항

- 집계는 **이 기능 배포 이후 쌓인 대화부터** 반영됩니다(과거 소급 없음).
- **차단(filtered)·상담원 연결** 답변은 규정 근거가 없어 집계에서 제외됩니다(정상 동작).

---

## 프론트 구현 체크리스트

- [ ] **대화 로그 화면**: 목록 테이블(작성자·질문·답변·차단 배지·시각) + 필터 UI(차단 토글 / 기간 / 키워드) + 페이징(20건)
- [ ] `filtered=true` 행은 "차단" 배지 등으로 시각 구분, 답변(거절 메시지)도 표시
- [ ] 작성자 표시 시 **null 폴백**: `userNickname ?? userName ?? '(알 수 없음)'`
- [ ] **RAG 채택 빈도 화면/위젯**: 문서·페이지별 `count` 랭킹(막대그래프/표) + 기간 필터
- [ ] 타입 정의 추가 (아래 참고)

### TypeScript 타입 참고

```ts
// 공통 래퍼
interface ApiResponse<T> {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  data: T;
}

interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ① 대화 로그
interface AdminChatLog {
  chatLogId: number;
  userId: number;
  userName: string | null;
  userNickname: string | null;
  question: string;
  answer: string;
  filtered: boolean;
  createdAt: string;
}

// ② RAG 채택 빈도
interface RagSourceStat {
  source: string;
  page: number | null;
  count: number;
}

// 호출 시그니처
// GET /api/v1/admin/chatbot/chat-logs        → ApiResponse<PageResponse<AdminChatLog>>
// GET /api/v1/admin/chatbot/rag-source-stats → ApiResponse<PageResponse<RagSourceStat>>
// GET /api/v1/admin/chatbot/chat-logs/export        → CSV 파일 (text/csv)
// GET /api/v1/admin/chatbot/rag-source-stats/export → CSV 파일 (text/csv)
```
