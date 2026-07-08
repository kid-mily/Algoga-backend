package com.kidmily.algoga_server.stats.application.usecase;

import com.kidmily.algoga_server.stats.presentation.api.response.LectureCountryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripByLectureResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface LectureToTripStatsUseCase {

    LectureToTripSummaryResponse getSummary(LocalDate from, LocalDate to);

    LectureToTripByLectureResponse getByLecture(LocalDate from, LocalDate to);

    List<LectureCountryResponse> getByCountry(LocalDate from, LocalDate to);

    byte[] getByCountryCsv(LocalDate from, LocalDate to);
}
