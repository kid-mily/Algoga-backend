package com.kidmily.algoga_server.course.statistics.application.usecase;

import com.kidmily.algoga_server.course.statistics.application.result.CourseEnrollmentStatisticsPageResult;

public interface CourseStatisticsUseCase {

    CourseEnrollmentStatisticsPageResult getCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    );
}