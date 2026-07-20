package com.kidmily.algoga_server.payment.application.usecase;

import com.kidmily.algoga_server.payment.application.command.CreateBundlePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentPreviewResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentResponse;

import java.util.List;

public interface PaymentCommandUseCase {
    Long handle(CreatePaymentCommand command);
    Long handleLecturePayment(CreateLecturePaymentCommand command);

    /** 패키지+강의 통합 결제 (PortOne 1회 결제 → 예약 결제 1건 + 강의 결제 N건 기록) */
    BundlePaymentResponse handleBundlePayment(CreateBundlePaymentCommand command);

    /** 통합 결제 사전 검증 — PortOne 결제창을 띄우기 전에 호출한다. DB를 바꾸지 않는다. */
    BundlePaymentPreviewResponse previewBundlePayment(Long bookingId, List<Long> courseIds, Long userId,
                                                      PaymentType paymentType, int usedMileage, Long usedCouponId);

    void handleWebhook(String portonePaymentId);
}