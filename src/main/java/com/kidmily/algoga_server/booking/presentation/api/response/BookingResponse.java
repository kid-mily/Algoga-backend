package com.kidmily.algoga_server.booking.presentation.api.response;

import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "예약 응답")
public record BookingResponse(

        @Schema(description = "예약 ID", example = "1")
        Long bookingId,

        @Schema(description = "숙소 ID", example = "1")
        Long accommodationId,

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "예약 상태", example = "PENDING")
        BookingStatus status,

        @Schema(description = "총 금액", example = "1200000")
        int totalPrice,

        @Schema(description = "예약금", example = "360000")
        int depositPrice,

        @Schema(description = "잔금", example = "840000")
        int balancePrice,

        @Schema(description = "예약 번호", example = "BK-20260523-00001")
        String bookingNumber,

        @Schema(description = "항공편 정보 (JSON)")
        String flightInfo,

        @Schema(description = "체크인 날짜", example = "2026-07-01")
        LocalDate checkInDate,

        @Schema(description = "체크아웃 날짜", example = "2026-07-04")
        LocalDate checkOutDate,

        @Schema(description = "숙박 박수", example = "3")
        int nights,

        @Schema(description = "예약 생성일시")
        LocalDateTime createdAt
) {
}