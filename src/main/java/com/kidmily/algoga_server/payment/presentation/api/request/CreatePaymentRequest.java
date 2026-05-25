package com.kidmily.algoga_server.payment.presentation.api.request;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull(message = "예약 ID는 필수입니다.")
        Long bookingId,

        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "결제 유형은 필수입니다.")
        PaymentType paymentType,

        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        int amount,

        int usedMileage,

        Long usedCouponId,

        @NotBlank(message = "PortOne 결제 ID는 필수입니다.")
        String portonePaymentId
) {}