package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsPageResult;

public interface CourseStatisticsUseCase {

    CourseEnrollmentStatisticsPageResult getCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    );
}