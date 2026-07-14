package com.kidmily.algoga_server.diagnosis.application.result;

import com.kidmily.algoga_server.course.application.result.CourseResult;
import java.time.LocalDateTime;
import java.util.List;

public record DiagnosisResultView(
        Long resultId,
        Long countryId,
        String countryName,
        Integer correctCount,
        Integer totalCount,
        Integer score,
        String level,
        String levelName,
        LocalDateTime submittedAt,
        List<DiagnosisAnswerResult> answers,
        List<CourseResult> recommendedCourses,
        List<CourseResult> otherLevelCourses
) {
}