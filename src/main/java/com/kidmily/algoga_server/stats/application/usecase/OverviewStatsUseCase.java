package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.domain.model.TrendUnit;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewTrendPointResponse;

import java.time.LocalDate;
import java.util.List;

public interface OverviewStatsUseCase {

    OverviewResponse getOverview(LocalDate from, LocalDate to);

    /** 총매출·환불·순매출 추이를 지정 단위(HOUR/DAY/MONTH)로 조회. 기간 프리셋별 그래프용. */
    List<OverviewTrendPointResponse> getTrend(LocalDate from, LocalDate to, TrendUnit unit);

    /** 월별 매출 상세(월/총매출/환불액/순매출/환불율/전월대비) CSV */
    byte[] getMonthlyCsv(LocalDate from, LocalDate to);
}
