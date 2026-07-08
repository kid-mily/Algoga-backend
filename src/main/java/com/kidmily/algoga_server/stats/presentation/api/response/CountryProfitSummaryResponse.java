package com.kidmily.algoga_server.stats.presentation.api.response;

public record CountryProfitSummaryResponse(
        int countryCount,
        long totalBookingCount,
        long totalNetRevenue,
        double avgRefundRate,
        String topCountryName,
        double topCountryShare
) {}
