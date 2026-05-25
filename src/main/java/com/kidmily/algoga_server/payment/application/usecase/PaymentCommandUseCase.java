package com.kidmily.algoga_server.payment.application.usecase;

import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;

public interface PaymentCommandUseCase {
    Long handle(CreatePaymentCommand command);
    void handleWebhook(String portonePaymentId);
}