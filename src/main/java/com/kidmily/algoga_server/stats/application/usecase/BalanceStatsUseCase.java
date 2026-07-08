package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.BalanceAgingResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.UnpaidBookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BalanceStatsUseCase {

    BalanceSummaryResponse getSummary(LocalDate from, LocalDate to);

    BalanceAgingResponse getAging(LocalDate from, LocalDate to);

    List<UnpaidBookingResponse> getUnpaidList(LocalDate from, LocalDate to);

    byte[] getUnpaidCsv(LocalDate from, LocalDate to);
}
