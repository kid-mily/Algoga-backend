package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.BalanceAgingResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.UnpaidBookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BalanceStatsUseCase {

    BalanceSummaryResponse getSummary(LocalDate from, LocalDate to);

    BalanceAgingResponse getAging(LocalDate from, LocalDate to);

    /** @param search 고객명 또는 상품명(숙소명) 부분 일치. null/빈값이면 전체 */
    List<UnpaidBookingResponse> getUnpaidList(LocalDate from, LocalDate to, String search);

    byte[] getUnpaidCsv(LocalDate from, LocalDate to, String search);
}
