package com.kidmily.algoga_server.lms.application.port;

import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsPageResult;

public interface CourseStatisticsQueryPort {

    CourseEnrollmentStatisticsPageResult findCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    );
}