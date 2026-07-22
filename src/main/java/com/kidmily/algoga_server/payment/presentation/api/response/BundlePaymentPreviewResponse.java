package com.kidmily.algoga_server.payment.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 패키지+강의 통합 결제 사전 검증 결과.
 * <p>
 * FE는 PortOne 결제창을 띄우기 <b>전에</b> 이 API를 호출해야 한다.
 * 통합 결제 본 API는 PortOne 결제가 끝난 뒤 검증하므로, 여기서 걸러내지 않으면
 * "돈은 빠져나갔는데 서버가 거부해서 기록이 안 남는" 상황이 생긴다.
 */
@Schema(description = "패키지+강의 통합 결제 사전 검증 결과")
public record BundlePaymentPreviewResponse(

        @Schema(description = "결제 진행 가능 여부. false면 결제창을 띄우지 말 것", example = "true")
        boolean payable,

        @Schema(description = "결제 불가 사유 코드. payable=true면 null",
                example = "DUPLICATE_PAYMENT",
                allowableValues = {"DUPLICATE_PAYMENT", "LECTURE_NOT_COMPLETED", "INSTALLMENT_NOT_ALLOWED",
                        "INVALID_PAYMENT_TYPE", "BOOKING_NOT_FOUND", "COURSE_NOT_FOUND", "COURSE_NOT_PUBLISHED",
                        "COUPON_INVALID", "INSUFFICIENT_MILEAGE", "INVALID_PAYMENT_AMOUNT",
                        "DEPARTURE_DATE_PASSED"})
        String blockReason,

        @Schema(description = "사람이 읽는 불가 사유. payable=true면 null")
        String blockMessage,

        @Schema(description = "이미 결제해서 다시 살 수 없는 강의 ID 목록. 이 값들을 courseIds에서 빼고 다시 호출할 것")
        List<Long> alreadyPaidCourseIds,

        @Schema(description = "패키지분 청구액 (쿠폰·마일리지 차감 후)", example = "276000")
        int packageAmount,

        @Schema(description = "강의 정가 합계 (이미 산 강의 제외). 쿠폰·마일리지 적용 대상 아님", example = "99000")
        int lectureAmount,

        @Schema(description = "PortOne에 청구해야 할 총액 = packageAmount + lectureAmount", example = "375000")
        int expectedTotal
) {

    public static BundlePaymentPreviewResponse blocked(String reason, String message,
                                                       List<Long> alreadyPaidCourseIds) {
        return new BundlePaymentPreviewResponse(
                false, reason, message,
                alreadyPaidCourseIds == null ? List.of() : alreadyPaidCourseIds,
                0, 0, 0);
    }

    public static BundlePaymentPreviewResponse payable(int packageAmount, int lectureAmount) {
        return new BundlePaymentPreviewResponse(
                true, null, null, List.of(),
                packageAmount, lectureAmount, packageAmount + lectureAmount);
    }
}
