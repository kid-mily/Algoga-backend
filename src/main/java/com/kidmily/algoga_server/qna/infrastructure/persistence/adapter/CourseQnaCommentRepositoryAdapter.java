package com.kidmily.algoga_server.qna.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.infrastructure.persistence.entity.CourseQnaCommentJpaEntity;
import com.kidmily.algoga_server.qna.infrastructure.persistence.repository.SpringDataCourseQnaCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseQnaCommentRepositoryAdapter implements CourseQnaCommentRepository {

    private final SpringDataCourseQnaCommentRepository springDataCourseQnaCommentRepository;

    @Override
    public CourseQnaComment save(CourseQnaComment comment) {
        CourseQnaCommentJpaEntity entity = springDataCourseQnaCommentRepository.findById(
                        comment.getId() == null ? -1L : comment.getId()
                )
                .map(existingEntity -> {
                    if (comment.isDeleted()) {
                        existingEntity.softDelete();
                    }

                    return existingEntity;
                })
                .orElseGet(() -> new CourseQnaCommentJpaEntity(
                        comment.getQnaId(),
                        comment.getParentCommentId(),
                        comment.getUserId(),
                        comment.getManagerId(),
                        comment.getWriterType(),
                        comment.getContent(),
                        comment.isDeleted(),
                        comment.getCreatedAt()
                ));

        CourseQnaCommentJpaEntity savedEntity = springDataCourseQnaCommentRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<CourseQnaComment> findByIdAndQnaId(Long commentId, Long qnaId) {
        return springDataCourseQnaCommentRepository.findByIdAndQnaId(commentId, qnaId)
                .map(this::toDomain);
    }

    @Override
    public List<CourseQnaComment> findByQnaId(Long qnaId) {
        return springDataCourseQnaCommentRepository.findByQnaIdAndDeletedFalseOrderByCreatedAtAsc(qnaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CourseQnaComment toDomain(CourseQnaCommentJpaEntity entity) {
        return CourseQnaComment.withId(
                entity.getId(),
                entity.getQnaId(),
                entity.getParentCommentId(),
                entity.getUserId(),
                entity.getManagerId(),
                entity.getWriterType(),
                entity.getContent(),
                entity.isDeleted(),
                entity.getCreatedAt()
        );
    }
}