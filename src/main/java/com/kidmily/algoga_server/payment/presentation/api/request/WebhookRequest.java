package com.kidmily.algoga_server.payment.presentation.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookRequest(
        String type,
        Data data
) {
    public record Data(
            @JsonProperty("paymentId")
            String paymentId,

            @JsonProperty("transactionId")
            String transactionId
    ) {}
}