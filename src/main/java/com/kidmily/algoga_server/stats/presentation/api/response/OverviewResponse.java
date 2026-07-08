package com.kidmily.algoga_server.stats.presentation.api.response;

import java.util.List;

public record OverviewResponse(
        long netRevenue,
        long totalRevenue,
        long totalRefund,
        long outstandingBalance,
        double refundRate,
        double balanceConversionRate,
        List<OverviewMonthlyResponse> monthly
) {}
