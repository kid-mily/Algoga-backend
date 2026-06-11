package com.kidmily.algoga_server.stats.presentation.api.response;

import java.util.List;

public record ConversionProductStatsResponse(
        List<ConversionProductResponse> products,
        List<ConversionProductResponse> topProducts,
        List<ConversionProductResponse> bottomProducts
) {
}
