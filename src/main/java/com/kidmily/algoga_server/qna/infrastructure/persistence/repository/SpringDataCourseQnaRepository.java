package com.kidmily.algoga_server.qna.infrastructure.persistence.repository;

import com.kidmily.algoga_server.qna.infrastructure.persistence.entity.CourseQnaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseQnaRepository extends JpaRepository<CourseQnaJpaEntity, Long> {

    Optional<CourseQnaJpaEntity> findByIdAndCourseId(Long id, Long courseId);

    List<CourseQnaJpaEntity> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}