package com.kidmily.algoga_server.itinerary.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ItineraryErrorCode implements BaseErrorCode {

    AI_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "ITN_001", "현재 AI 일정 생성 서버 연결이 원활하지 않습니다. 잠시 후 다시 시도해주세요."),
    NON_PACKAGE_INPUT_REQUIRED(HttpStatus.BAD_REQUEST, "ITN_002", "자유 여행(FREE)은 목적지와 여행 기간(시작일·종료일)을 입력해야 합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "ITN_003", "여행 종료일은 시작일과 같거나 이후여야 합니다."),
    ITINERARY_NOT_FOUND(HttpStatus.NOT_FOUND, "ITN_004", "해당 일정 추천 내역을 찾을 수 없습니다."),
    PACKAGE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ITN_005", "패키지 여행(PACKAGE)은 packageId 가 필요합니다."),
    BOOKING_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ITN_006", "구매 여행(BOOKING)은 bookingId 가 필요합니다."),
    BOOKING_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ITN_007", "선택한 예약을 찾을 수 없거나 일정 추천에 사용할 수 없는 예약입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
