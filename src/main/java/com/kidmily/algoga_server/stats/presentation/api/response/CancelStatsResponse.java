package com.kidmily.algoga_server.stats.presentation.api.response;

public record CancelStatsResponse(
        long totalBookings,
        long cancelCount,
        double cancelRate,
        long unpaidCancel,
        long depositCancel,
        long fullCancel,
        double paidCancelRate
) {
    public static CancelStatsResponse of(long totalBookings, long unpaidCancel,
                                         long depositCancel, long fullCancel) {
        long cancelCount = unpaidCancel + depositCancel + fullCancel;
        long paidCancel = depositCancel + fullCancel;
        double cancelRate = totalBookings == 0 ? 0.0
                : Math.round((double) cancelCount / totalBookings * 10000.0) / 100.0;
        double paidCancelRate = totalBookings == 0 ? 0.0
                : Math.round((double) paidCancel / totalBookings * 10000.0) / 100.0;
        return new CancelStatsResponse(totalBookings, cancelCount, cancelRate,
                unpaidCancel, depositCancel, fullCancel, paidCancelRate);
    }
}
