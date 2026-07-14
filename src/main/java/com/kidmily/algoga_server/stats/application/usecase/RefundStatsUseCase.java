package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.domain.model.TrendUnit;
import com.kidmily.algoga_server.stats.presentation.api.response.*;

import java.time.LocalDate;
import java.util.List;

public interface RefundStatsUseCase {

    RefundSummaryResponse getSummary(LocalDate from, LocalDate to);

    List<RefundTrendResponse> getTrend(LocalDate from, LocalDate to);

    /** 총매출·환불·순매출 추이를 지정 단위(HOUR/DAY/MONTH)로 버킷팅해 반환한다. */
    List<OverviewTrendPointResponse> getTrend(LocalDate from, LocalDate to, TrendUnit unit);

    List<RefundTimingResponse> getTiming(LocalDate from, LocalDate to);

    List<RefundByCountryResponse> getByCountry(LocalDate from, LocalDate to);

    List<RefundReasonResponse> getReasons(LocalDate from, LocalDate to);

    CancelStatsResponse getCancelStats(LocalDate from, LocalDate to);
}
