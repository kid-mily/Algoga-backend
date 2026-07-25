package com.kidmily.algoga_server.stats.presentation.api.response;

public record ConversionSummaryResponse(
        long attemptCount,
        long completedCount,
        double conversionRate
) {
    public static ConversionSummaryResponse of(long attemptCount, long completedCount) {
        long boundedCompletedCount = Math.min(completedCount, attemptCount);
        double rate = attemptCount == 0 ? 0.0 : Math.round((double) boundedCompletedCount / attemptCount * 10000.0) / 100.0;
        return new ConversionSummaryResponse(attemptCount, completedCount, rate);
    }
}