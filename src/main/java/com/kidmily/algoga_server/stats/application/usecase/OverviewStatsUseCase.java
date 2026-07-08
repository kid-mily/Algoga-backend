package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.OverviewResponse;

import java.time.LocalDate;

public interface OverviewStatsUseCase {

    OverviewResponse getOverview(LocalDate from, LocalDate to);
}
