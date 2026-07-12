package com.kidmily.algoga_server.course.statistics.application.service;

import com.kidmily.algoga_server.course.statistics.application.port.CourseStatisticsQueryPort;
import com.kidmily.algoga_server.course.statistics.application.result.CourseEnrollmentStatisticsPageResult;
import com.kidmily.algoga_server.course.statistics.application.usecase.CourseStatisticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseStatisticsService implements CourseStatisticsUseCase {

    private final CourseStatisticsQueryPort courseStatisticsQueryPort;

    @Override
    public CourseEnrollmentStatisticsPageResult getCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    ) {
        return courseStatisticsQueryPort.findCourseEnrollmentStatistics(keyword, page, size);
    }
}