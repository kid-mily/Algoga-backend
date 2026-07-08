package com.kidmily.algoga_server.stats.presentation.api.response;

import java.util.List;

public record LectureToTripByLectureResponse(
        List<LectureConversionResponse> top,
        List<LectureConversionResponse> bottom
) {}
