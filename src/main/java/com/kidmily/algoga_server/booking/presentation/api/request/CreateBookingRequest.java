package com.kidmily.algoga_server.booking.presentation.api.request;

import com.kidmily.algoga_server.booking.domain.model.BookingSource;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "예약 생성 요청")
public record CreateBookingRequest(
        @Schema(description = "숙소 ID", example = "1")
        @NotNull(message = "숙소 ID는 필수입니다.")
        Long accommodationId,

        @Schema(description = "가는편 항공편 정보")
        @NotNull(message = "항공편 정보는 필수입니다.")
        FlightInfoRequest flightInfo,

        @Schema(description = "오는편 항공편 정보 (패키지 예약 시 패키지의 returnFlightInfo를 그대로 전달. 없으면 생략 가능)")
        FlightInfoRequest returnFlightInfo,

        @Schema(description = "탑승객(여권) 정보 — 성/이름/생년월일/여권번호/만료일")
        PassengerInfoRequest passengerInfo,

        @Schema(description = "항공편 가격 (왕복 합산가)", example = "600000")
        @Positive
        int flightPrice,

        @Schema(description = "체크인 날짜", example = "2026-07-01")
        @NotNull
        LocalDate checkInDate,

        @Schema(description = "체크아웃 날짜", example = "2026-07-04")
        @NotNull
        LocalDate checkOutDate,

        @Schema(description = "예약 진입 경로. LOUNGE=패키지 라운지(분할/일시불 선택), "
                + "COMPLETION=완강 후 마이페이지 모달(완강 필수·일시불 고정). 미전달 시 LOUNGE로 처리",
                example = "LOUNGE")
        BookingSource bookingSource
) {
    /**
     * 여권 유효성 교차검증: 여권 만료일이 귀국일(checkOutDate)보다 빠르면 안 된다.
     * 여권/만료일/귀국일 중 하나라도 없으면 이 검증은 통과(필수 여부는 별도 영역).
     */
    @Schema(hidden = true, accessMode = AccessMode.READ_ONLY)
    @AssertTrue(message = "여권 만료일이 귀국일보다 빠릅니다. 여행 종료일까지 유효한 여권이 필요합니다.")
    public boolean isPassportValidForTravel() {
        if (passengerInfo == null || passengerInfo.passportExpiry() == null || checkOutDate == null) {
            return true;
        }
        return !passengerInfo.passportExpiry().isBefore(checkOutDate);
    }
}