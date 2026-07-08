package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.*;

import java.time.LocalDate;
import java.util.List;

public interface RefundStatsUseCase {

    RefundSummaryResponse getSummary(LocalDate from, LocalDate to);

    List<RefundTrendResponse> getTrend(LocalDate from, LocalDate to);

    List<RefundTimingResponse> getTiming(LocalDate from, LocalDate to);

    List<RefundByCountryResponse> getByCountry(LocalDate from, LocalDate to);

    List<RefundReasonResponse> getReasons(LocalDate from, LocalDate to);

    CancelStatsResponse getCancelStats(LocalDate from, LocalDate to);
}
