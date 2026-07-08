package com.kidmily.algoga_server.stats.presentation.api.response;

public record LectureCountryResponse(
        Long countryId,
        String countryName,
        long buyers,
        long completed,
        long converted,
        double conversionRate
) {
    public static LectureCountryResponse of(Long countryId, String countryName,
                                            long buyers, long completed, long converted) {
        double rate = completed == 0 ? 0.0 : Math.round((double) converted / completed * 10000.0) / 100.0;
        return new LectureCountryResponse(countryId, countryName, buyers, completed, converted, rate);
    }
}
