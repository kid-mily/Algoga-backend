package com.kidmily.algoga_server.course.statistics.application.result;

public record CourseEnrollmentStatisticsResult(
        Long courseId,
        String courseTitle,
        long studentCount,
        int averageProgressRate,
        int completionRate,
        double averageLearningHours
) {
}