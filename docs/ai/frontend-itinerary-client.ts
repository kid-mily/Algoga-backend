/**
 * AI 일정 추천(Itinerary) — 프론트 연동용 타입 & API 클라이언트 (드롭인)
 *
 * 이 파일 하나로 요청/응답 타입과 호출 함수를 모두 제공한다. 프레임워크 비종속(fetch 기반).
 * 상세 계약은 docs/ai/frontend-itinerary-api.md 참고.
 *
 * 사용 예)
 *   const trips = await getPurchasedTrips();
 *   const result = await recommendItinerary({
 *     tripType: "BOOKING", bookingId: trips[0].bookingId,
 *     preferences: ["FOOD"], purpose: "SIGHTSEEING", companion: "COUPLE",
 *     budget: 1_000_000, headcount: 2,
 *   });
 */

// ────────────────────────────────────────────────────────────
// 0. 공통
// ────────────────────────────────────────────────────────────

/** 배포 환경에 맞게 교체(프록시 쓰면 "" 로 두고 상대경로 사용). */
const BASE_URL = "/api/v1";

/** 성공 응답 봉투. */
export interface ApiResponse<T> {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  data: T;
}

/** 에러 응답 봉투. */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  errorCode: string; // 예: "ITN_006"
  message: string;
  traceId: string;
}

/** API 에러. errorCode 로 분기하고, traceId 는 로깅에 사용. */
export class ItineraryApiError extends Error {
  constructor(
    readonly status: number,
    readonly errorCode: string,
    message: string,
    readonly traceId?: string,
  ) {
    super(message);
    this.name = "ItineraryApiError";
  }
}

// ────────────────────────────────────────────────────────────
// 1. Enum & 라벨 (서버 전송은 code, 화면 표시는 label)
// ────────────────────────────────────────────────────────────

export type TripType = "BOOKING" | "PACKAGE" | "FREE";
export const TRIP_TYPE_LABEL: Record<TripType, string> = {
  BOOKING: "구매한 패키지",
  PACKAGE: "전체 패키지",
  FREE: "자유 여행",
};

export type TravelPreference =
  | "NATURE" | "FOOD" | "ACTIVITY" | "RELAXATION" | "SHOPPING" | "CULTURE" | "PHOTO";
export const PREFERENCE_LABEL: Record<TravelPreference, string> = {
  NATURE: "자연", FOOD: "맛집", ACTIVITY: "액티비티", RELAXATION: "휴양",
  SHOPPING: "쇼핑", CULTURE: "문화", PHOTO: "사진",
};

export type TravelPurpose =
  | "RELAXATION" | "SIGHTSEEING" | "GOURMET" | "ACTIVITY" | "ANNIVERSARY" | "ETC";
export const PURPOSE_LABEL: Record<TravelPurpose, string> = {
  RELAXATION: "휴양", SIGHTSEEING: "관광", GOURMET: "미식",
  ACTIVITY: "액티비티", ANNIVERSARY: "기념일", ETC: "기타",
};

export type Companion = "ALONE" | "COUPLE" | "FRIENDS" | "FAMILY" | "WITH_KIDS";
export const COMPANION_LABEL: Record<Companion, string> = {
  ALONE: "혼자", COUPLE: "연인", FRIENDS: "친구", FAMILY: "가족", WITH_KIDS: "아이동반",
};

/** 예약 상태(구매 여행 목록). */
export type BookingStatus = "PENDING" | "DEPOSIT_PAID" | "FULL_PAID";
export const BOOKING_STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: "결제대기", DEPOSIT_PAID: "예약금 결제", FULL_PAID: "결제완료",
};

// ────────────────────────────────────────────────────────────
// 2. 요청/응답 타입
// ────────────────────────────────────────────────────────────

/** 항상 필수인 사용자 직접입력. */
interface RecommendCommon {
  preferences: TravelPreference[]; // 최소 1개
  purpose: TravelPurpose;
  companion: Companion;
  budget: number;   // 총예산(원), 0 초과
  headcount: number; // 1 이상
}

/** tripType 별 요청 (판별 유니온 — 셋 중 하나의 식별자만 채운다). */
export type RecommendItineraryRequest =
  | ({ tripType: "BOOKING"; bookingId: number } & RecommendCommon)
  | ({ tripType: "PACKAGE"; packageId: number } & RecommendCommon)
  | ({ tripType: "FREE"; destination: string; startDate: string; endDate: string } & RecommendCommon);

export interface PreferenceView { code: TravelPreference; label: string; }

export interface EstimatedCost {
  /** 패키지 조회가/예약 결제금액(항공+숙소). 자유여행이면 null. */
  packagePrice: number | null;
  foodCost: number;       // AI 추정 음식비(여행 전체 총액)
  totalEstimated: number; // packagePrice(없으면 0) + foodCost
}

export interface ItinerarySlot {
  time: "오전" | "오후" | "저녁";
  activity: string;
  place: string;
  memo: string | null; // null 이면 메모 줄 숨김
}
export interface ItineraryDay { day: number; date: string; slots: ItinerarySlot[]; }

/** 일정 추천 상세 응답. */
export interface ItineraryResponse {
  itineraryId: number;
  destination: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  headcount: number;
  packageTrip: boolean; // BOOKING·PACKAGE=true, FREE=false
  purpose: TravelPurpose;
  purposeLabel: string;
  companion: Companion;
  companionLabel: string;
  preferences: PreferenceView[];
  budget: number;
  estimatedCost: EstimatedCost;
  days: ItineraryDay[];
  comment: string;
  createdAt: string;
}

/** 이력 목록 요약(슬롯 없음). */
export interface ItinerarySummaryResponse {
  itineraryId: number;
  destination: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  packageTrip: boolean;
  purpose: TravelPurpose;
  purposeLabel: string;
  estimatedTotalCost: number;
  createdAt: string;
}

/** 구매(예약) 여행 선택지 — tripType=BOOKING 용. */
export interface PurchasedTripResponse {
  bookingId: number;        // ← recommend 의 bookingId 로 그대로 사용
  destination: string;      // 국가명(조회 실패 시 숙소명)
  accommodationName: string | null;
  startDate: string;
  endDate: string;
  nights: number;
  price: number;            // 결제 총액(원) = 추천의 packagePrice
  status: BookingStatus;
  bookingNumber: string;
}

/** 전체 패키지 선택지 — tripType=PACKAGE 용. 항공편 조회 없이 경량. */
export interface SelectablePackageResponse {
  packageId: number;        // ← recommend 의 packageId 로 그대로 사용
  name: string;             // 패키지명
  destination: string | null; // 국가명(조회 실패 시 null)
  startDate: string;
  endDate: string;
  nights: number;
  price: number;            // 등록된 패키지 기본가(원) — 표시용
  imageUrl: string;
}

// ────────────────────────────────────────────────────────────
// 3. HTTP 헬퍼
// ────────────────────────────────────────────────────────────

/**
 * 인증 토큰 주입 지점. 프로젝트의 토큰 저장소에 맞게 교체하세요.
 * (쿠키 세션이면 이 함수 대신 credentials:"include" 를 request 에 추가)
 */
let getAccessToken: () => string | null = () => null;
export function setAccessTokenProvider(fn: () => string | null) {
  getAccessToken = fn;
}

async function request<T>(
  path: string,
  init: RequestInit & { timeoutMs?: number } = {},
): Promise<T> {
  const { timeoutMs, ...rest } = init;
  const controller = new AbortController();
  const timer = timeoutMs ? setTimeout(() => controller.abort(), timeoutMs) : null;

  const token = getAccessToken();
  try {
    const res = await fetch(`${BASE_URL}${path}`, {
      ...rest,
      signal: controller.signal,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(rest.headers ?? {}),
      },
    });

    const body = await res.json().catch(() => null);
    if (!res.ok) {
      const err = body as ErrorResponse | null;
      throw new ItineraryApiError(
        res.status,
        err?.errorCode ?? String(res.status),
        err?.message ?? "요청 처리에 실패했습니다.",
        err?.traceId,
      );
    }
    return (body as ApiResponse<T>).data;
  } finally {
    if (timer) clearTimeout(timer);
  }
}

// ────────────────────────────────────────────────────────────
// 4. API
// ────────────────────────────────────────────────────────────

/**
 * AI 일정 추천 생성. LLM 호출이라 수 초 걸릴 수 있어 타임아웃을 60초로 둔다.
 * 실패 시 ItineraryApiError(errorCode: ITN_001/002/003/005/006/007 ...) 를 던진다.
 */
export function recommendItinerary(req: RecommendItineraryRequest): Promise<ItineraryResponse> {
  return request<ItineraryResponse>("/itineraries/recommend", {
    method: "POST",
    body: JSON.stringify(req),
    timeoutMs: 60_000,
  });
}

/** 내가 구매한(추천에 사용 가능한) 여행 목록. tripType=BOOKING 선택지. 취소/환불 예약 제외. */
export function getPurchasedTrips(): Promise<PurchasedTripResponse[]> {
  return request<PurchasedTripResponse[]>("/itineraries/purchased-trips");
}

/** 전체 패키지 선택 목록. tripType=PACKAGE 선택지. 항공편 조회 없이 경량이라 빠름. */
export function getSelectablePackages(): Promise<SelectablePackageResponse[]> {
  return request<SelectablePackageResponse[]>("/itineraries/selectable-packages");
}

/** 내 일정 추천 이력(요약, 최신순). */
export function getMyItineraries(): Promise<ItinerarySummaryResponse[]> {
  return request<ItinerarySummaryResponse[]>("/itineraries");
}

/** 일정 추천 상세(본인 소유만). 없으면 ItineraryApiError(errorCode: ITN_004, 404). */
export function getItinerary(id: number): Promise<ItineraryResponse> {
  return request<ItineraryResponse>(`/itineraries/${id}`);
}

// ────────────────────────────────────────────────────────────
// 5. 폼 선검증 (서버 왕복 전에 막을 것들)
// ────────────────────────────────────────────────────────────

/** 반환값이 있으면 그 메시지를 폼 에러로 노출. null 이면 통과. */
export function validateRecommend(
  form: Partial<RecommendItineraryRequest> & { tripType?: TripType },
): string | null {
  if (!form.tripType) return "여행 유형을 선택하세요.";
  if (!form.preferences?.length) return "취향을 최소 1개 선택하세요.";
  if (form.tripType === "BOOKING" && (form as any).bookingId == null)
    return "구매한 여행을 선택하세요."; // ITN_006 예방
  if (form.tripType === "PACKAGE" && (form as any).packageId == null)
    return "패키지를 선택하세요."; // ITN_005 예방
  if (form.tripType === "FREE") {
    const f = form as any;
    if (!f.destination?.trim() || !f.startDate || !f.endDate)
      return "여행지·시작일·종료일을 입력하세요."; // ITN_002 예방
    if (f.endDate < f.startDate) return "종료일은 시작일과 같거나 이후여야 합니다."; // ITN_003 예방
  }
  if (form.budget != null && form.budget <= 0) return "예산은 0보다 커야 합니다.";
  if (form.headcount != null && form.headcount < 1) return "인원수는 1명 이상이어야 합니다.";
  return null;
}
