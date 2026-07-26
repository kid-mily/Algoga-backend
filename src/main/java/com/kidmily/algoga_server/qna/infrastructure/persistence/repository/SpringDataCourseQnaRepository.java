package com.kidmily.algoga_server.qna.infrastructure.persistence.repository;

import com.kidmily.algoga_server.qna.infrastructure.persistence.entity.CourseQnaJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseQnaRepository extends JpaRepository<CourseQnaJpaEntity, Long> {

    Optional<CourseQnaJpaEntity> findByIdAndCourseId(Long id, Long courseId);

    Page<CourseQnaJpaEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    // 어드민 전체 강의 통합 Q&A 목록 검색. 각 파라미터가 null 이면 해당 조건을 건너뛴다.
    @Query(value = "SELECT q FROM CourseQnaJpaEntity q "
            + "WHERE (:courseId IS NULL OR q.courseId = :courseId) "
            + "AND (:status IS NULL OR q.status = :status) "
            + "AND (:keyword IS NULL "
            + "     OR q.title LIKE CONCAT('%', :keyword, '%') "
            + "     OR q.question LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY q.createdAt DESC",
            countQuery = "SELECT COUNT(q) FROM CourseQnaJpaEntity q "
            + "WHERE (:courseId IS NULL OR q.courseId = :courseId) "
            + "AND (:status IS NULL OR q.status = :status) "
            + "AND (:keyword IS NULL "
            + "     OR q.title LIKE CONCAT('%', :keyword, '%') "
            + "     OR q.question LIKE CONCAT('%', :keyword, '%'))")
    Page<CourseQnaJpaEntity> searchForAdmin(
            @Param("courseId") Long courseId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT q.id FROM CourseQnaJpaEntity q WHERE q.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}