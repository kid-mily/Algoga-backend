package com.kidmily.algoga_server.completion.domain.repository;

import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseCompletionRepository {

    CourseCompletion save(CourseCompletion courseCompletion);

    Optional<CourseCompletion> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseCompletion> findByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

    // 여러 유저 × 여러 강의의 완강 기록을 한 번에 조회 (통계에서 (유저,강의)별 exists 반복 = N+1 제거용)
    List<CourseCompletion> findByUserIdInAndCourseIdIn(List<Long> userIds, List<Long> courseIds);

    List<CourseCompletion> findByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Map<Long, Long> countByCourseIds(List<Long> courseIds);

    void deleteAllByUserId(Long userId);
}
