package com.kidmily.algoga_server.stats.presentation.api.response;

public record LectureToTripSummaryResponse(
        long lectureBuyers,
        long completedCount,
        double completionRate,
        long convertedCount,
        double completedConversionRate,
        double notCompletedConversionRate,
        double vsNonCompletedMultiple
) {
    public static LectureToTripSummaryResponse of(long buyers, long completed, long completedConverted,
                                                  long notCompletedConverted) {
        long notCompleted = buyers - completed;
        double completionRate = pct(completed, buyers);
        double completedConvRate = pct(completedConverted, completed);
        double notCompletedConvRate = pct(notCompletedConverted, notCompleted);
        double multiple = notCompletedConvRate == 0 ? 0.0
                : Math.round(completedConvRate / notCompletedConvRate * 10.0) / 10.0;
        return new LectureToTripSummaryResponse(buyers, completed, completionRate,
                completedConverted, completedConvRate, notCompletedConvRate, multiple);
    }

    private static double pct(long part, long total) {
        return total == 0 ? 0.0 : Math.round((double) part / total * 10000.0) / 100.0;
    }
}
