package com.kidmily.algoga_server.itinerary.presentation.api.response;

import com.kidmily.algoga_server.itinerary.application.result.PurchasedTrip;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * "내가 구매한 여행" 선택지 응답. tripType=BOOKING 으로 일정 추천 시 bookingId 를 그대로 넘긴다.
 */
@Schema(description = "일정 추천에 사용할 수 있는 내 구매(예약) 여행")
public record PurchasedTripResponse(

        @Schema(description = "예약 ID. 추천 요청 시 tripType=BOOKING 의 bookingId 로 전송", example = "34")
        Long bookingId,

        @Schema(description = "목적지(국가명). 국가 조회 실패 시 숙소명으로 대체", example = "일본")
        String destination,

        @Schema(description = "숙소 이름", example = "신주쿠 프린스 호텔", nullable = true)
        String accommodationName,

        @Schema(description = "여행 시작일(체크인)", example = "2026-08-01")
        LocalDate startDate,

        @Schema(description = "여행 종료일(체크아웃)", example = "2026-08-03")
        LocalDate endDate,

        @Schema(description = "숙박 박수", example = "2")
        int nights,

        @Schema(description = "결제 총액(원). 일정 추천의 패키지가격으로 사용", example = "770000")
        int price,

        @Schema(description = "예약 상태", example = "FULL_PAID")
        String status,

        @Schema(description = "예약 번호", example = "BK-20260523-00001")
        String bookingNumber
) {
    public static PurchasedTripResponse from(PurchasedTrip trip) {
        return new PurchasedTripResponse(
                trip.bookingId(),
                trip.destination(),
                trip.accommodationName(),
                trip.startDate(),
                trip.endDate(),
                trip.nights(),
                trip.price(),
                trip.status(),
                trip.bookingNumber()
        );
    }
}
