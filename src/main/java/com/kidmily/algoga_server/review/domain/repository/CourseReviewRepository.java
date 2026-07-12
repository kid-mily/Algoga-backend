package com.kidmily.algoga_server.review.domain.repository;

import com.kidmily.algoga_server.review.domain.model.CourseReview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CourseReviewRepository {

    CourseReview save(CourseReview courseReview);

    Optional<CourseReview> findByIdAndCourseId(Long reviewId, Long courseId);

    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseId(Long courseId);

    List<CourseReview> findAllByCourseId(Long courseId);

    Map<Long, Double> findAverageRatingsByCourseIds(List<Long> courseIds);

    Set<Long> findReviewedCourseIdsByUserIdAndCourseIds(Long userId, List<Long> courseIds);

    boolean existsByUserIdAndCourseIdAndDeletedFalse(Long userId, Long courseId);

    void deleteHiddenBefore(LocalDateTime threshold);
}
