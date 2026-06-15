package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.ConversionDailyResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionProductStatsResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ConversionStatsUseCase {
    void recordAttempt(Long userId, Long accommodationId);
    ConversionSummaryResponse getSummary(LocalDate from, LocalDate to);
    List<ConversionDailyResponse> getDailyStats(LocalDate from, LocalDate to);
    ConversionProductStatsResponse getProductStats(LocalDate from, LocalDate to);
}
