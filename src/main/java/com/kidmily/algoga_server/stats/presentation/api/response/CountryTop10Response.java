package com.kidmily.algoga_server.stats.presentation.api.response;

import java.util.List;

public record CountryTop10Response(
        List<CountryStatsItemResponse> bookingTop10,
        List<CountryStatsItemResponse> revenueTop10
) {
}
