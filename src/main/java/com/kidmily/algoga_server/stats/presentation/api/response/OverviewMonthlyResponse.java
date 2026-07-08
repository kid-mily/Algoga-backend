package com.kidmily.algoga_server.stats.presentation.api.response;

public record OverviewMonthlyResponse(
        String month,
        long revenue,
        long refund,
        long net,
        double refundRate,
        Double growthRate
) {}
