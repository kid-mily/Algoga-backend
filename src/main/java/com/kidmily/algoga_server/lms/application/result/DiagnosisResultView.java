package com.kidmily.algoga_server.lms.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record DiagnosisResultView(
        Long resultId,
        Long countryId,
        Integer correctCount,
        Integer totalCount,
        Integer score,
        String level,
        String levelName,
        LocalDateTime submittedAt,
        List<DiagnosisAnswerResult> answers,
        List<CourseResult> recommendedCourses
) {
}
