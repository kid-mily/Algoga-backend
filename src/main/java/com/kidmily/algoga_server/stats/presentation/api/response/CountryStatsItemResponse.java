package com.kidmily.algoga_server.stats.presentation.api.response;

public record CountryStatsItemResponse(
        Long countryId,
        String countryName,
        String countryCode,
        long signupCount,
        long bookingCount,
        long revenue,
        double shareRate
) {
    public static CountryStatsItemResponse of(Long countryId, String countryName, String countryCode,
                                               long bookingCount, long revenue, long totalRevenue) {
        double share = totalRevenue == 0 ? 0.0 : Math.round((double) revenue / totalRevenue * 10000.0) / 100.0;
        return new CountryStatsItemResponse(countryId, countryName, countryCode, 0L, bookingCount, revenue, share);
    }
}
