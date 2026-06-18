package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsPageResult;
import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "수강률 통계 응답")
public record CourseEnrollmentStatisticsResponse(
        long totalElements,
        int page,
        int size,
        List<CourseEnrollmentStatisticsItemResponse> content
) {
    public static CourseEnrollmentStatisticsResponse from(CourseEnrollmentStatisticsPageResult result) {
        return new CourseEnrollmentStatisticsResponse(
                result.totalElements(),
                result.page(),
                result.size(),
                result.content().stream()
                        .map(CourseEnrollmentStatisticsItemResponse::from)
                        .toList()
        );
    }

    public record CourseEnrollmentStatisticsItemResponse(
            Long courseId,
            String courseTitle,
            long studentCount,
            int averageProgressRate,
            int completionRate,
            double averageLearningHours
    ) {
        public static CourseEnrollmentStatisticsItemResponse from(CourseEnrollmentStatisticsResult result) {
            return new CourseEnrollmentStatisticsItemResponse(
                    result.courseId(),
                    result.courseTitle(),
                    result.studentCount(),
                    result.averageProgressRate(),
                    result.completionRate(),
                    result.averageLearningHours()
            );
        }
    }
}