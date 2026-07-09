package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.CohortResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RetentionSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.TopCustomerResponse;

import java.time.LocalDate;
import java.util.List;

public interface RetentionStatsUseCase {

    RetentionSummaryResponse getSummary(LocalDate from, LocalDate to);

    List<TopCustomerResponse> getTopCustomers(LocalDate from, LocalDate to);

    byte[] getTopCustomersCsv(LocalDate from, LocalDate to);

    CohortResponse getCohort(LocalDate from, LocalDate to);
}
