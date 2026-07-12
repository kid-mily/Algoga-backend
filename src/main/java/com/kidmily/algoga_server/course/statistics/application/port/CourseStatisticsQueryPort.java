package com.kidmily.algoga_server.course.statistics.application.port;

import com.kidmily.algoga_server.course.statistics.application.result.CourseEnrollmentStatisticsPageResult;

public interface CourseStatisticsQueryPort {

    CourseEnrollmentStatisticsPageResult findCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    );
}