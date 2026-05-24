package com.kidmily.algoga_server.lms.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.ChapterJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.repository.SpringDataChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChapterRepositoryAdapter implements ChapterRepository {

    private final SpringDataChapterRepository springDataChapterRepository;

    @Override
    public Chapter save(Chapter chapter) {
        ChapterJpaEntity entity = new ChapterJpaEntity(
                chapter.getCourseId(),
                chapter.getTitle(),
                chapter.getVideoUrl(),
                chapter.getDurationSeconds(),
                chapter.getChapterOrder()
        );

        ChapterJpaEntity savedEntity = springDataChapterRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Chapter> findByCourseId(Long courseId) {
        return springDataChapterRepository.findByCourseIdAndDeletedFalseOrderByOrderNumAsc(courseId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Chapter> findByIdAndCourseId(Long chapterId, Long courseId) {
        return springDataChapterRepository.findByIdAndCourseIdAndDeletedFalse(chapterId, courseId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Chapter> updateBasicInfo(
            Long chapterId,
            Long courseId,
            String title,
            String videoUrl,
            int durationSeconds,
            int chapterOrder
    ) {
        return springDataChapterRepository.findByIdAndCourseIdAndDeletedFalse(chapterId, courseId)
                .map(entity -> {
                    entity.updateBasicInfo(
                            title,
                            videoUrl,
                            durationSeconds,
                            chapterOrder
                    );

                    return toDomain(entity);
                });
    }

    @Override
    public boolean softDelete(Long chapterId, Long courseId) {
        return springDataChapterRepository.findByIdAndCourseIdAndDeletedFalse(chapterId, courseId)
                .map(entity -> {
                    entity.softDelete();
                    return true;
                })
                .orElse(false);
    }

    private Chapter toDomain(ChapterJpaEntity entity) {
        return Chapter.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getTitle(),
                entity.getVideoUrl(),
                entity.getDurationSeconds(),
                entity.getOrderNum(),
                entity.isDeleted()
        );
    }
}