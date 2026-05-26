package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.CourseReview;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository {

    CourseReview save(CourseReview courseReview);

    Optional<CourseReview> findByIdAndCourseId(Long reviewId, Long courseId);

    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseId(Long courseId);

    boolean existsByUserIdAndCourseIdAndDeletedFalse(Long userId, Long courseId);
}