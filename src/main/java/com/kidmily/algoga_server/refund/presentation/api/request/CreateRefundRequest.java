package com.kidmily.algoga_server.refund.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRefundRequest(
        @NotNull(message = "예약 ID는 필수입니다.")
        Long bookingId,

        @NotNull(message = "결제 ID는 필수입니다.")
        Long paymentId,

        @NotBlank(message = "환불 사유는 필수입니다.")
        String reason
) {}