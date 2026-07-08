package com.kidmily.algoga_server.stats.presentation.api.response;

public record BalanceSummaryResponse(
        double balanceConversionRate,
        long outstandingAmount,
        long depositPaidCount,
        long fullPaidCount,
        long atRiskCount,
        long ddayImminentCount
) {
    public static BalanceSummaryResponse of(long depositPaidCount, long fullPaidCount,
                                            long outstandingAmount, long atRiskCount,
                                            long ddayImminentCount) {
        long denom = depositPaidCount + fullPaidCount;
        double rate = denom == 0 ? 0.0
                : Math.round((double) fullPaidCount / denom * 10000.0) / 100.0;
        return new BalanceSummaryResponse(rate, outstandingAmount, depositPaidCount,
                fullPaidCount, atRiskCount, ddayImminentCount);
    }
}
