package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.port.CourseStatisticsQueryPort;
import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsPageResult;
import com.kidmily.algoga_server.lms.application.usecase.CourseStatisticsUseCase;
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