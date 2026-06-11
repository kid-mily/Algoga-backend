package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.CountryStatsItemResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryTop10Response;

import java.time.LocalDate;
import java.util.List;

public interface CountryStatsUseCase {

    List<CountryStatsItemResponse> getCountryStats(LocalDate from, LocalDate to, String search);

    CountryTop10Response getTop10(LocalDate from, LocalDate to);

    byte[] getCsvExport(LocalDate from, LocalDate to);
}
