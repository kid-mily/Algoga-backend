package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface CountryProfitStatsUseCase {

    CountryProfitSummaryResponse getSummary(LocalDate from, LocalDate to);

    List<CountryProfitResponse> getList(LocalDate from, LocalDate to);

    byte[] getCsv(LocalDate from, LocalDate to);
}
