package com.kidmily.algoga_server.payment.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "패키지+강의 통합 결제 응답")
public record BundlePaymentResponse(

        @Schema(description = "예약(패키지) 결제 ID", example = "10")
        Long bookingPaymentId,

        @Schema(description = "강의 결제 ID 목록 (요청한 courseIds 순서)", example = "[11, 12]")
        List<Long> lecturePaymentIds,

        @Schema(description = "패키지분 결제 금액 (예약금 또는 전액, 쿠폰·마일리지 차감 후)", example = "231000")
        int bookingAmount,

        @Schema(description = "강의 결제 금액 합계 (정가 합)", example = "700000")
        int lectureAmount,

        @Schema(description = "총 결제 금액 (bookingAmount + lectureAmount)", example = "931000")
        int totalAmount
) {}
