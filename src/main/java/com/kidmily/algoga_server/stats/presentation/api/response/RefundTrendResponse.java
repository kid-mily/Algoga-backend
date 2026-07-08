package com.kidmily.algoga_server.stats.presentation.api.response;

public record RefundTrendResponse(
        String month,
        long revenue,
        long refund,
        long net
) {}
