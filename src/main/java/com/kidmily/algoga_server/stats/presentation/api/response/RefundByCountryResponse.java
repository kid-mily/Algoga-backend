package com.kidmily.algoga_server.stats.presentation.api.response;

public record RefundByCountryResponse(
        Long countryId,
        String countryName,
        long refundCount,
        long refundAmount
) {}
