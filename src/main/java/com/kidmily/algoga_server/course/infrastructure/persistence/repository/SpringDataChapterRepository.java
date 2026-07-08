package com.kidmily.algoga_server.course.infrastructure.persistence.repository;

import com.kidmily.algoga_server.course.infrastructure.persistence.entity.ChapterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataChapterRepository extends JpaRepository<ChapterJpaEntity, Long> {

    List<ChapterJpaEntity> findByCourseIdAndDeletedFalseOrderByOrderNumAsc(Long courseId);

    List<ChapterJpaEntity> findByCourseIdInAndDeletedFalseOrderByCourseIdAscOrderNumAsc(List<Long> courseIds);

    Optional<ChapterJpaEntity> findByIdAndCourseIdAndDeletedFalse(Long id, Long courseId);

    long countByCourseIdAndDeletedFalse(Long courseId);

    boolean existsByCourseIdAndOrderNumAndDeletedFalse(Long courseId, int orderNum);

    boolean existsByCourseIdAndOrderNumAndIdNotAndDeletedFalse(
            Long courseId,
            int orderNum,
            Long id
    );
}

