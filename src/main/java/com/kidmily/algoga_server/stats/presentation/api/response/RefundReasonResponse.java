package com.kidmily.algoga_server.stats.presentation.api.response;

public record RefundReasonResponse(
        String reason,
        long count,
        long amount
) {}
