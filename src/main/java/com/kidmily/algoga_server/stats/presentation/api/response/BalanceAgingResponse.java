package com.kidmily.algoga_server.stats.presentation.api.response;

import java.util.List;

public record BalanceAgingResponse(
        List<AgingPoint> curve,
        List<CountryBalance> byCountry
) {
    public record AgingPoint(
            int daysSinceDeposit,
            double cumulativePaidRate
    ) {}

    public record CountryBalance(
            Long countryId,
            String countryName,
            long depositPaidCount,
            long fullPaidCount,
            double conversionRate
    ) {
        public static CountryBalance of(Long countryId, String countryName,
                                        long depositPaidCount, long fullPaidCount) {
            long denom = depositPaidCount + fullPaidCount;
            double rate = denom == 0 ? 0.0
                    : Math.round((double) fullPaidCount / denom * 10000.0) / 100.0;
            return new CountryBalance(countryId, countryName, depositPaidCount, fullPaidCount, rate);
        }
    }
}
