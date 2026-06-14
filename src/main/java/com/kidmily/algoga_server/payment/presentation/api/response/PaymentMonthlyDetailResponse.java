package com.kidmily.algoga_server.payment.presentation.api.response;

import java.util.List;

public record PaymentMonthlyDetailResponse(
        int year,
        int month,
        int totalAmount,
        int refundAmount,
        int netAmount,
        long count,
        Double growthRate,
        List<DailyStats> dailyStats
) {
    public record DailyStats(
            int day,
            int salesAmount,
            int refundAmount,
            int netAmount
    ) {}
}
