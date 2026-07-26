package com.kidmily.algoga_server.qna.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.qna.domain.model.CourseQna;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.qna.infrastructure.persistence.entity.CourseQnaJpaEntity;
import com.kidmily.algoga_server.qna.infrastructure.persistence.repository.SpringDataCourseQnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseQnaRepositoryAdapter implements CourseQnaRepository {

    private final SpringDataCourseQnaRepository springDataCourseQnaRepository;

    @Override
    public CourseQna save(CourseQna courseQna) {
        CourseQnaJpaEntity entity = springDataCourseQnaRepository.findById(courseQna.getId() == null ? -1L : courseQna.getId())
                .map(existingEntity -> {
                    if ("ANSWERED".equals(courseQna.getStatus())) {
                        existingEntity.answer(
                                courseQna.getManagerId(),
                                courseQna.getAnswer(),
                                courseQna.getAnsweredAt()
                        );
                    }

                    return existingEntity;
                })
                .orElseGet(() -> new CourseQnaJpaEntity(
                        courseQna.getCourseId(),
                        courseQna.getUserId(),
                        courseQna.getManagerId(),
                        courseQna.getTitle(),
                        courseQna.getQuestion(),
                        courseQna.getAnswer(),
                        courseQna.getStatus(),
                        courseQna.getCreatedAt(),
                        courseQna.getAnsweredAt()
                ));

        CourseQnaJpaEntity savedEntity = springDataCourseQnaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<CourseQna> findById(Long qnaId) {
        return springDataCourseQnaRepository.findById(qnaId)
                .map(this::toDomain);
    }

    @Override
    public Optional<CourseQna> findByIdAndCourseId(
            Long qnaId,
            Long courseId
    ) {
        return springDataCourseQnaRepository.findByIdAndCourseId(qnaId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Page<CourseQna> findByCourseId(Long courseId, Pageable pageable) {
        return springDataCourseQnaRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<Long> findIdsByUserId(Long userId) {
        return springDataCourseQnaRepository.findIdsByUserId(userId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataCourseQnaRepository.deleteByUserId(userId);
    }

    private CourseQna toDomain(CourseQnaJpaEntity entity) {
        return CourseQna.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getUserId(),
                entity.getManagerId(),
                entity.getTitle(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getAnsweredAt()
        );
    }
}