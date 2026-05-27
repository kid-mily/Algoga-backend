package com.kidmily.algoga_server.payment.presentation.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLecturePaymentRequest(
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId,

        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        int amount,

        int usedMileage,

        Long usedCouponId,

        @NotBlank(message = "PortOne 결제 ID는 필수입니다.")
        String portonePaymentId
) {}