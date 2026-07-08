package com.kidmily.algoga_server.stats.presentation.api.response;

public record RefundSummaryResponse(
        double refundRate,
        long netRevenue,
        long bookingRevenue,
        long totalRefund,
        long refundCount,
        long avgRefundAmount
) {
    public static RefundSummaryResponse of(long bookingRevenue, long totalRefund, long refundCount) {
        double rate = bookingRevenue == 0 ? 0.0
                : Math.round((double) totalRefund / bookingRevenue * 10000.0) / 100.0;
        long avg = refundCount == 0 ? 0 : totalRefund / refundCount;
        return new RefundSummaryResponse(rate, bookingRevenue - totalRefund,
                bookingRevenue, totalRefund, refundCount, avg);
    }
}
