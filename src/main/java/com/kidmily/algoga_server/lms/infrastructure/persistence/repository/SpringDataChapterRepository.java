package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.ChapterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataChapterRepository extends JpaRepository<ChapterJpaEntity, Long> {

    List<ChapterJpaEntity> findByCourseIdAndDeletedFalseOrderByOrderNumAsc(Long courseId);

    Optional<ChapterJpaEntity> findByIdAndCourseIdAndDeletedFalse(Long id, Long courseId);
}