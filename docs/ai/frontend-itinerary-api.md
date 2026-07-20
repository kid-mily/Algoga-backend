# AI 일정 추천 — 프론트엔드 연동 명세

> AI/프론트가 이 문서만 보고 바로 구현할 수 있도록 작성한 API 계약서.
> 대상 도메인: **AI 일정 추천(Itinerary)** — 사용자 입력 + 자동수집(강의·예약·패키지) 기반 일자별 여행 일정 생성.

---

## 0. 공통 규약

- **Base URL**: `/api/v1`
- **Content-Type**: `application/json`
- **인증**: 모든 엔드포인트 **로그인 필수**(JWT). 미인증 시 `401`. userId는 서버가 토큰에서 확정하므로 프론트가 보내지 않는다.
- **날짜 형식**: 날짜만 `YYYY-MM-DD`(예: `2026-08-01`), 일시는 ISO-8601 UTC.

### 성공 응답 봉투 `ApiResponse<T>`
```jsonc
{
  "timestamp": "2026-07-17T02:56:48Z",
  "status": 200,
  "code": "ITINERARY_CREATED",
  "message": "AI 일정 추천이 생성되었습니다.",
  "data": { /* 실제 페이로드 */ }
}
```

### 에러 응답 봉투 `ErrorResponse`
```jsonc
{
  "timestamp": "2026-07-17T02:56:48Z",
  "status": 400,
  "errorCode": "ITN_002",
  "message": "패키지 여행이 아닌 경우 목적지와 여행 기간(시작일·종료일)을 입력해야 합니다.",
  "traceId": "6c98f70b"
}
```

---

## 1. Enum 레퍼런스 (프론트 상수화용)

**서버 전송은 `code`(enum name), 화면 표시는 라벨.** 응답에는 code·label이 함께 내려온다.

```ts
// 여행 유형 (단일선택, 프론트가 진입 맥락으로 분기)
type TripType = "BOOKING" | "PACKAGE" | "FREE";
const TRIP_TYPE_LABEL = {
  BOOKING: "구매한 패키지", PACKAGE: "전체 패키지", FREE: "자유 여행",
} as const;

// 여행 취향 (다중선택, 최소 1개)
type TravelPreference = "NATURE" | "FOOD" | "ACTIVITY" | "RELAXATION" | "SHOPPING" | "CULTURE" | "PHOTO";
const PREFERENCE_LABEL = {
  NATURE: "자연", FOOD: "맛집", ACTIVITY: "액티비티", RELAXATION: "휴양",
  SHOPPING: "쇼핑", CULTURE: "문화", PHOTO: "사진",
} as const;

// 여행 목적 (단일선택)
type TravelPurpose = "RELAXATION" | "SIGHTSEEING" | "GOURMET" | "ACTIVITY" | "ANNIVERSARY" | "ETC";
const PURPOSE_LABEL = {
  RELAXATION: "휴양", SIGHTSEEING: "관광", GOURMET: "미식",
  ACTIVITY: "액티비티", ANNIVERSARY: "기념일", ETC: "기타",
} as const;

// 동행자 (단일선택)
type Companion = "ALONE" | "COUPLE" | "FRIENDS" | "FAMILY" | "WITH_KIDS";
const COMPANION_LABEL = {
  ALONE: "혼자", COUPLE: "연인", FRIENDS: "친구", FAMILY: "가족", WITH_KIDS: "아이동반",
} as const;
```

---

## 2. 일정 추천 생성 ⭐

`POST /api/v1/itineraries/recommend`

### 요청 바디
```jsonc
{
  // ── 여행 유형 (프론트가 명시 분기) ──
  "tripType": "BOOKING",        // "BOOKING" | "PACKAGE" | "FREE"

  // tripType=BOOKING 일 때 (구매한 여행)
  "bookingId": 34,              // 이 값으로 목적지·기간·결제금액을 서버가 조회해 채움

  // tripType=PACKAGE 일 때 (전체 패키지 카탈로그)
  "packageId": 12,              // 이 값으로 목적지·기간·가격을 서버가 조회해 채움

  // tripType=FREE 일 때 (자유 여행)
  "destination": "일본 오사카",  // FREE 시 필수
  "startDate": "2026-08-01",    // FREE 시 필수
  "endDate": "2026-08-03",      // FREE 시 필수

  // ── 사용자 직접입력 (항상 필수) ──
  "preferences": ["FOOD", "NATURE"],  // 최소 1개
  "purpose": "SIGHTSEEING",
  "companion": "COUPLE",
  "budget": 1000000,            // 총예산(원)
  "headcount": 2                // 인원수(1 이상)
}
```

> `tripType`별로 셋 중 **하나의 식별자만** 보내면 된다: `BOOKING`→`bookingId`, `PACKAGE`→`packageId`, `FREE`→`destination`+기간. 나머지 필드는 보내도 무시됨.

### 필드 규칙
| 필드 | 필수 | 규칙 |
|---|---|---|
| `tripType` | ✅ | `"BOOKING"` \| `"PACKAGE"` \| `"FREE"` |
| `preferences` | ✅ | `TravelPreference[]`, 최소 1개 |
| `purpose` | ✅ | `TravelPurpose` |
| `companion` | ✅ | `Companion` |
| `budget` | ✅ | 0보다 큰 정수(원) |
| `headcount` | ✅ | 1 이상 |
| `bookingId` | 조건부 | **`tripType=BOOKING`이면 필수** |
| `packageId` | 조건부 | **`tripType=PACKAGE`이면 필수** |
| `destination` `startDate` `endDate` | 조건부 | **`tripType=FREE`이면 필수** |

### 응답 `data` = `ItineraryResponse`
```jsonc
{
  "itineraryId": 1,
  "destination": "일본 오사카",
  "startDate": "2026-08-01",
  "endDate": "2026-08-03",
  "totalDays": 3,
  "headcount": 2,
  "packageTrip": true,           // 서버가 자동 판별한 패키지 여행 여부
  "purpose": "SIGHTSEEING",
  "purposeLabel": "관광",
  "companion": "COUPLE",
  "companionLabel": "연인",
  "preferences": [
    { "code": "FOOD", "label": "맛집" },
    { "code": "NATURE", "label": "자연" }
  ],
  "budget": 1000000,
  "estimatedCost": {
    "packagePrice": 770000,      // 패키지 조회가/예약 결제금액(항공+숙소). 자유여행이면 null
    "foodCost": 180000,          // AI 추정 음식비(여행 전체 총액)
    "totalEstimated": 950000     // packagePrice(없으면 0) + foodCost
  },
  "days": [
    {
      "day": 1,
      "date": "2026-08-01",
      "slots": [
        { "time": "오전", "activity": "도톤보리 산책", "place": "도톤보리", "memo": "지하철 5분" },
        { "time": "오후", "activity": "오사카성 관람", "place": "오사카성", "memo": "입장권 예매 권장" },
        { "time": "저녁", "activity": "타코야키 맛집", "place": "난바", "memo": null }
      ]
    }
    // day 2, day 3 ...
  ],
  "comment": "예산 내에서 여유로운 일정입니다. 8월은 더우니 오후 실내 위주로 조정하세요."
}
```

### 에러
| errorCode | HTTP | 상황 |
|---|---|---|
| `ITN_002` | 400 | `tripType=FREE`인데 `destination`/`startDate`/`endDate` 누락 |
| `ITN_003` | 400 | `endDate`가 `startDate`보다 이전 |
| `ITN_005` | 400 | `tripType=PACKAGE`인데 `packageId` 누락 |
| `ITN_006` | 400 | `tripType=BOOKING`인데 `bookingId` 누락 |
| `ITN_007` | 400 | 내 예약이 아니거나 사용할 수 없는(취소·환불) 예약 |
| `ITN_001` | 503 | AI 생성 서버(파이썬) 연결 실패/지연 — 잠시 후 재시도 안내 |
| 공통 validation | 400 | `tripType` 누락, `preferences` 비었거나 `budget`/`headcount` 규칙 위반. `message`에 `필드: 사유` |

> 생성은 LLM 호출이라 수 초 걸릴 수 있음 → 프론트는 **로딩 UI + 타임아웃 여유(최대 60초)** 두기.

---

## 3. 여행 유형 분기 (3-모드) ⭐

**프론트가 `tripType`으로 명시 분기**한다. 백엔드는 선언을 받아 검증·조회한다.
일정 추천 화면에서 사용자는 **① 내가 구매한 여행 / ② 전체 패키지 / ③ 자유 여행** 중 하나로 진입한다.

### `tripType: "BOOKING"` — 내가 구매한 여행
- **[3-1 구매 여행 목록](#3-1-구매-여행-목록-조회)** 에서 하나를 골라 그 **`bookingId`만** 넘긴다.
- 서버가 그 예약을 조회해 **목적지(숙소가 속한 국가명)·기간(checkIn~checkOut)·결제금액(totalPrice)** 을 자동으로 채운다.
- `estimatedCost.packagePrice`에 예약 결제금액이 들어옴.
- 본인 소유 예약만 사용 가능(서버가 토큰으로 검증). 취소요청·환불 완료 예약은 목록에서 제외됨.
- `bookingId` 누락 시 `ITN_006`, 내 예약이 아니거나 사용 불가 예약이면 `ITN_007`.

### `tripType: "PACKAGE"` — 전체 패키지 카탈로그
- **[전체 패키지 목록](#3-2-전체-패키지-목록-조회)** 에서 하나를 골라 그 **`packageId`만** 넘긴다 (아직 구매하지 않은 상품도 추천 가능).
- 서버가 그 패키지를 조회해 **목적지(국가명)·기간(checkIn~checkOut)·가격(totalPrice)** 을 자동으로 채운다.
- `estimatedCost.packagePrice`에 패키지 조회가격이 들어옴.
- `packageId` 누락 시 `ITN_005`.

### `tripType: "FREE"` — 자유 여행
- **`destination`, `startDate`, `endDate` 반드시 입력** (누락 시 `ITN_002`).
- 패키지 가격이 없으므로 `packagePrice`는 `null`, 비용은 음식비 위주로 추정됨.

> `BOOKING`·`PACKAGE`는 응답에서 `packageTrip=true`, `FREE`는 `false`.

**프론트 구현 팁**
```ts
// ① 내가 구매한 여행 → 목록에서 선택한 bookingId만
recommend({ tripType: "BOOKING", bookingId, preferences, purpose, companion, budget, headcount });

// ② 전체 패키지 → 목록에서 선택한 packageId만
recommend({ tripType: "PACKAGE", packageId, preferences, purpose, companion, budget, headcount });

// ③ 자유여행(직접 계획) → 목적지·날짜 입력받아 전송
recommend({ tripType: "FREE", destination, startDate, endDate, preferences, purpose, companion, budget, headcount });
```

### 3-1. 구매 여행 목록 조회
`GET /api/v1/itineraries/purchased-trips`
- `tripType=BOOKING` 선택지를 채우기 위한 목록. **로그인 사용자의 예약** 중 추천에 쓸 수 있는 것만 최신순으로 내려준다.
- 취소요청(`CANCEL_REQUESTED`)·환불 완료(`REFUNDED`) 예약은 **제외**.
- `data`: `PurchasedTripResponse[]`
```jsonc
[
  {
    "bookingId": 34,                       // ← recommend 요청의 bookingId 로 그대로 사용
    "destination": "일본",                  // 숙소가 속한 국가명(조회 실패 시 숙소명으로 대체)
    "accommodationName": "신주쿠 프린스 호텔", // 없으면 null
    "startDate": "2026-08-01",             // 체크인
    "endDate": "2026-08-03",               // 체크아웃
    "nights": 2,
    "price": 770000,                       // 결제 총액(원) = 추천의 packagePrice
    "status": "FULL_PAID",                 // PENDING | DEPOSIT_PAID | FULL_PAID
    "bookingNumber": "BK-20260523-00001"
  }
  // ...
]
```
> 목록이 비어 있으면(구매 이력 없음) "패키지를 먼저 예약해 주세요" 같은 빈 상태 UI를 노출하고, 전체 패키지(②)나 자유여행(③)으로 유도한다.

### 3-2. 전체 패키지 목록 조회
`GET /api/v1/packages` *(패키지 도메인 기존 API — 이 도메인 소관 아님)*
- `tripType=PACKAGE` 선택지를 채우는 카탈로그. 각 항목의 `packageId`를 recommend에 사용.
- 나라별로 좁히려면 `GET /api/v1/countries/{countryId}/packages`.

---

## 4. 내 일정 추천 이력

### 4-1. 목록 (요약)
`GET /api/v1/itineraries`
- `data`: `ItinerarySummaryResponse[]` (최신순)
```jsonc
{
  "itineraryId": 1,
  "destination": "일본 오사카",
  "startDate": "2026-08-01",
  "endDate": "2026-08-03",
  "totalDays": 3,
  "packageTrip": true,
  "purpose": "SIGHTSEEING",
  "purposeLabel": "관광",
  "estimatedTotalCost": 950000,
  "createdAt": "2026-07-17T02:56:48Z"
}
```
> 목록에는 상세 `slots`가 없다(가벼움). 상세는 4-2로 조회.

### 4-2. 상세
`GET /api/v1/itineraries/{id}`
- `data`: `ItineraryResponse` (2장 응답과 동일 구조, `days` 포함)
- 본인 소유만 조회 가능. 아니거나 없으면 `404` `ITN_004`.

---

## 5. 자동수집 데이터 (프론트가 신경 안 써도 되는 부분)

아래는 **서버가 알아서 수집·반영**한다. 프론트가 보낼 필요 없음.
- **강의 이력** → 학습한 나라를 "관심 국가"로 간주해 추천에 반영
- **구매 여행**(`tripType=BOOKING`) → `bookingId` 조회로 목적지 국가명 + 기간 + 결제금액 자동 확정
- **패키지 내역**(`tripType=PACKAGE`) → `packageId` 조회로 목적지 국가명 + 기간 + 패키지 가격 자동 확정
- 개인 결제정보는 외부 AI로 전송하지 않음

---

## 6. 구현 체크리스트

- [ ] 요청은 enum `code`로 전송, 화면은 label로 표시 (`preferences`는 배열)
- [ ] `tripType` 3-모드 분기: 구매여행(=`bookingId`) / 전체패키지(=`packageId`) / 자유여행(=`destination`+기간)
- [ ] `BOOKING`·`PACKAGE`는 목록 조회 → 선택 UI로 식별자 확정 (구매목록: `GET /itineraries/purchased-trips`, 전체: `GET /packages`)
- [ ] 구매 목록이 비면 빈 상태 UI + 전체패키지/자유여행으로 유도
- [ ] 생성 API는 수 초 소요 → 로딩 스피너 + 60초 타임아웃
- [ ] 폼 선검증: `ITN_006`(bookingId)·`ITN_005`(packageId)·`ITN_002`(FREE 입력)·`ITN_003`(날짜 역전)
- [ ] `ITN_007`(사용 불가 예약)·`ITN_001`(503) 수신 시 사용자 안내/재시도 UI
- [ ] 결과 렌더: `days[].slots[]`를 오전/오후/저녁 카드로, `estimatedCost` 요약, `comment` 노출
- [ ] `estimatedCost.packagePrice`가 `null`이면(자유여행) 음식비 위주로 표시
- [ ] 목록/상세 분리 조회 (목록엔 slots 없음)
- [ ] 에러 시 `traceId` 로깅
