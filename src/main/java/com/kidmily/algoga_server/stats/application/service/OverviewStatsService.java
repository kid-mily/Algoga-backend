package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.stats.application.usecase.BalanceStatsUseCase;
import com.kidmily.algoga_server.stats.application.usecase.OverviewStatsUseCase;
import com.kidmily.algoga_server.stats.application.usecase.RefundStatsUseCase;
import com.kidmily.algoga_server.stats.domain.model.TrendUnit;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewMonthlyResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.OverviewTrendPointResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RefundSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.RefundTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * ① 돈 요약 대시보드
 * - ②잔금·미수금 + ③환불 요약을 조합해 경영진용 KPI + 월별 추이를 구성 (계산 로직 재사용)
 */
@Service
@RequiredArgsConstructor
public class OverviewStatsService implements OverviewStatsUseCase {

    private final RefundStatsUseCase refundStatsUseCase;
    private final BalanceStatsUseCase balanceStatsUseCase;

    @Override
    @Transactional(readOnly = true)
    public OverviewResponse getOverview(LocalDate from, LocalDate to) {
        RefundSummaryResponse refund = refundStatsUseCase.getSummary(from, to);
        BalanceSummaryResponse balance = balanceStatsUseCase.getSummary(from, to);
        List<RefundTrendResponse> trend = refundStatsUseCase.getTrend(from, to);

        List<OverviewMonthlyResponse> monthly = new ArrayList<>();
        Long prevNet = null;
        for (RefundTrendResponse t : trend) {
            double refundRate = t.revenue() == 0 ? 0.0
                    : Math.round((double) t.refund() / t.revenue() * 10000.0) / 100.0;
            Double growthRate = (prevNet == null || prevNet == 0) ? null
                    : Math.round((double) (t.net() - prevNet) / prevNet * 10000.0) / 100.0;
            monthly.add(new OverviewMonthlyResponse(t.month(), t.revenue(), t.refund(), t.net(), refundRate, growthRate));
            prevNet = t.net();
        }

        return new OverviewResponse(
                refund.netRevenue(),
                refund.bookingRevenue(),
                refund.totalRefund(),
                balance.outstandingAmount(),
                refund.refundRate(),
                balance.balanceConversionRate(),
                monthly);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverviewTrendPointResponse> getTrend(LocalDate from, LocalDate to, TrendUnit unit) {
        return refundStatsUseCase.getTrend(from, to, unit);
    }
}
