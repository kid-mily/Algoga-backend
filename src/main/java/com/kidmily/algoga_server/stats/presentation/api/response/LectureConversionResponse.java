package com.kidmily.algoga_server.stats.presentation.api.response;

public record LectureConversionResponse(
        Long lectureId,
        String title,
        long buyers,
        long completed,
        long converted,
        double conversionRate
) {
    public static LectureConversionResponse of(Long lectureId, String title,
                                               long buyers, long completed, long converted) {
        double rate = completed == 0 ? 0.0 : Math.round((double) converted / completed * 10000.0) / 100.0;
        return new LectureConversionResponse(lectureId, title, buyers, completed, converted, rate);
    }
}
