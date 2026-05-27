package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.CourseReward;

import java.util.Optional;

public interface CourseRewardRepository {

    CourseReward save(CourseReward courseReward);

    Optional<CourseReward> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}