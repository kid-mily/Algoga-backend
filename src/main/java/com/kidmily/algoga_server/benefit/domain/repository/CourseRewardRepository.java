package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.CourseReward;

import java.util.Optional;

public interface CourseRewardRepository {

    CourseReward save(CourseReward courseReward);

    Optional<CourseReward> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    void deleteAllByUserId(Long userId);
}