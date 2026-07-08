package com.kidmily.algoga_server.stats.presentation.api.response;

public record RefundTimingResponse(
        String bucket,
        int policyRate,
        long count,
        long amount
) {}
