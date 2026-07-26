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

    @Query("SELECT q.id FROM CourseQnaJpaEntity q WHERE q.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}