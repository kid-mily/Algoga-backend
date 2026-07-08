package com.kidmily.algoga_server.stats.presentation.api.response;

public record RetentionSummaryResponse(
        double repeatRate,
        long arpu,
        double avgIntervalDays,
        double top10RevenueShare
) {}
