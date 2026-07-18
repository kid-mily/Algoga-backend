package com.kidmily.algoga_server.payment.application.usecase;

import com.kidmily.algoga_server.payment.application.command.CreateBundlePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentResponse;

public interface PaymentCommandUseCase {
    Long handle(CreatePaymentCommand command);
    Long handleLecturePayment(CreateLecturePaymentCommand command);

    /** 패키지+강의 통합 결제 (PortOne 1회 결제 → 예약 결제 1건 + 강의 결제 N건 기록) */
    BundlePaymentResponse handleBundlePayment(CreateBundlePaymentCommand command);

    void handleWebhook(String portonePaymentId);
}