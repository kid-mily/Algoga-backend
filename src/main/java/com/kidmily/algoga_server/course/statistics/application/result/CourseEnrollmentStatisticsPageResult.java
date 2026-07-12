package com.kidmily.algoga_server.course.statistics.application.result;

import java.util.List;

public record CourseEnrollmentStatisticsPageResult(
        long totalElements,
        int page,
        int size,
        List<CourseEnrollmentStatisticsResult> content
) {
}