package com.kidmily.algoga_server.payment.presentation.api.response;

public record PaymentStatsResponse(
        int year,
        int month,
        int totalAmount,
        long count,
        int refundAmount,
        int netAmount,
        Double growthRate
) {}