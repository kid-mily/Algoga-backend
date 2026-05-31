package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseQnaCommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseQnaCommentRepository extends JpaRepository<CourseQnaCommentJpaEntity, Long> {

    Optional<CourseQnaCommentJpaEntity> findByIdAndQnaId(Long id, Long qnaId);

    List<CourseQnaCommentJpaEntity> findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(Long qnaId);
}