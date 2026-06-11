package com.kidmily.algoga_server.stats.presentation.api.response;

import java.time.LocalDate;

public record ConversionDailyResponse(
        LocalDate date,
        long attemptCount,
        long completedCount,
        double conversionRate
) {
    public static ConversionDailyResponse of(LocalDate date, long attemptCount, long completedCount) {
        double rate = attemptCount == 0 ? 0.0 : Math.round((double) completedCount / attemptCount * 10000.0) / 100.0;
        return new ConversionDailyResponse(date, attemptCount, completedCount, rate);
    }
}
