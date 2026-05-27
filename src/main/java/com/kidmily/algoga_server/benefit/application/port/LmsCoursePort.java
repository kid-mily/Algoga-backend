package com.kidmily.algoga_server.benefit.application.port;

import java.util.Optional;

public interface LmsCoursePort {

    void validateCourseExists(Long courseId);

    CourseRewardInfo getCourseRewardInfo(Long userId, Long courseId);

    Optional<CourseSummary> findCourseSummary(Long courseId);

    boolean existsCountry(Long countryId);

    record CourseRewardInfo(
            Long courseId,
            Integer coursePrice,
            int correctCount
    ) {
    }

    record CourseSummary(
            Long courseId,
            String courseTitle,
            Long countryId,
            String countryName
    ) {
    }
}