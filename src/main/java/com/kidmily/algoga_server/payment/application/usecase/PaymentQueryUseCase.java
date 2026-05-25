package com.kidmily.algoga_server.payment.application.usecase;

import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;

public interface PaymentQueryUseCase {
    PaymentResponse getPayment(Long paymentId);
    byte[] getConfirmationPdf(Long paymentId);
}