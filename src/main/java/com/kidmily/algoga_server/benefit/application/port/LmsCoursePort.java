package com.kidmily.algoga_server.benefit.application.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LmsCoursePort {

    void validateCourseExists(Long courseId);

    CourseRewardInfo getCourseRewardInfo(Long userId, Long courseId);

    Optional<CourseSummary> findCourseSummary(Long courseId);

    Map<Long, CourseSummary> findCourseSummaries(List<Long> courseIds);

    boolean existsCountry(Long countryId);

    record CourseRewardInfo(
            Long courseId,
            Integer coursePrice,
            Integer maxRewardMileage,
            int correctCount,
            LocalDateTime enrolledAt
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
