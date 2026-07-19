package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface CountryProfitStatsUseCase {

    CountryProfitSummaryResponse getSummary(LocalDate from, LocalDate to);

    /** @param search 국가명 부분 일치. null/빈값이면 전체 */
    List<CountryProfitResponse> getList(LocalDate from, LocalDate to, String search);

    byte[] getCsv(LocalDate from, LocalDate to, String search);
}
