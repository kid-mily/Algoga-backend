package com.kidmily.algoga_server.stats.presentation.api.response;

public record ConversionProductResponse(
        Long accommodationId,
        String productName,
        long attemptCount,
        long completedCount,
        double conversionRate
) {
    public static ConversionProductResponse of(Long accommodationId, String productName,
                                                long attemptCount, long completedCount) {
        double rate = attemptCount == 0 ? 0.0 : Math.round((double) completedCount / attemptCount * 10000.0) / 100.0;
        return new ConversionProductResponse(accommodationId, productName, attemptCount, completedCount, rate);
    }
}
