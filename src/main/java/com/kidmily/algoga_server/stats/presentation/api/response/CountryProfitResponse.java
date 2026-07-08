package com.kidmily.algoga_server.stats.presentation.api.response;

public record CountryProfitResponse(
        Long countryId,
        String countryName,
        long bookingCount,
        long grossRevenue,
        long netRevenue,
        double refundRate,
        double balanceConversionRate,
        double cancelRate,
        double share
) {}
