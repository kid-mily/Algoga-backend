package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseQnaJpaEntity;
import com.kidmily.algoga_server.lms.tdd.SpringDataCourseQnaRepository;
import lombok.RequiredArgsConstructor;
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
    public List<CourseQna> findByCourseId(Long courseId) {
        return springDataCourseQnaRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
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