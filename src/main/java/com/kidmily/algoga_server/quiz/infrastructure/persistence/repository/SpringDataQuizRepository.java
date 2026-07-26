package com.kidmily.algoga_server.quiz.infrastructure.persistence.repository;

import com.kidmily.algoga_server.quiz.infrastructure.persistence.entity.QuizJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataQuizRepository extends JpaRepository<QuizJpaEntity, Long> {

    List<QuizJpaEntity> findByCourseIdAndDeletedFalseOrderByIdAsc(Long courseId);

    long countByCourseIdAndDeletedFalse(Long courseId);

    Optional<QuizJpaEntity> findByIdAndCourseIdAndDeletedFalse(Long id, Long courseId);

    // 어드민 전체 강의 통합 퀴즈 목록 검색. 각 파라미터가 null 이면 해당 조건을 건너뛴다.
    @Query(value = "SELECT q FROM QuizJpaEntity q "
            + "WHERE q.deleted = false "
            + "AND (:courseId IS NULL OR q.courseId = :courseId) "
            + "AND (:keyword IS NULL OR q.question LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY q.id DESC",
            countQuery = "SELECT COUNT(q) FROM QuizJpaEntity q "
            + "WHERE q.deleted = false "
            + "AND (:courseId IS NULL OR q.courseId = :courseId) "
            + "AND (:keyword IS NULL OR q.question LIKE CONCAT('%', :keyword, '%'))")
    Page<QuizJpaEntity> searchForAdmin(
            @Param("courseId") Long courseId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
