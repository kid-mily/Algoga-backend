package com.kidmily.algoga_server.payment.presentation.api.request;

import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "패키지+강의 통합 결제 요청 (PortOne 결제 1회로 예약 결제 + 강의 결제를 함께 처리)")
public record CreateBundlePaymentRequest(

        @Schema(description = "예약 ID (POST /api/v1/bookings 응답의 bookingId)", example = "1")
        @NotNull(message = "예약 ID는 필수입니다.")
        Long bookingId,

        @Schema(description = "함께 결제할 강의 ID 목록. 이미 결제한 강의(isPaid=true)는 제외하고 보내야 합니다.",
                example = "[1, 2]")
        @NotEmpty(message = "강의 ID 목록은 비어 있을 수 없습니다.")
        List<Long> courseIds,

        @Schema(description = "패키지 결제 방식. DEPOSIT=분할(예약금 30%), FULL=일시불(전액). "
                + "강의는 분할 개념이 없어 항상 전액입니다.", example = "DEPOSIT")
        @NotNull(message = "결제 유형은 필수입니다.")
        PaymentType paymentType,

        @Schema(description = "총 결제 금액 = 패키지분(예약금 또는 전액) + 강의 정가 합계 - 쿠폰할인 - 마일리지",
                example = "931000")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        int amount,

        @Schema(description = "사용 마일리지 (패키지분에서만 차감)", example = "0")
        int usedMileage,

        @Schema(description = "사용 쿠폰 ID (패키지분에만 적용)")
        Long usedCouponId,

        @Schema(description = "PortOne 결제 ID (통합 결제 1회의 결제 ID)")
        @NotBlank(message = "PortOne 결제 ID는 필수입니다.")
        String portonePaymentId
) {}
