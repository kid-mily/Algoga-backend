package com.kidmily.algoga_server.lms.infrastructure.mapper;

import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.ChapterJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    // 1. Domain -> DB Entity 변환 (저장할 때)
    public CourseJpaEntity toEntity(Course course) {
        CourseJpaEntity courseEntity = new CourseJpaEntity(
                course.getCountryId(),
                course.getManagerId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                course.getStatus()
        );

        // 챕터들도 순회하면서 Entity로 변환하여 추가
        if (course.getChapters() != null) {
            course.getChapters().forEach(chapter -> {
                ChapterJpaEntity chapterEntity = new ChapterJpaEntity(
                        chapter.getTitle(),
                        chapter.getVideoUrl(),
                        chapter.getDurationSeconds(),
                        chapter.getChapterOrder()
                );
                courseEntity.getChapters().add(chapterEntity);
            });
        }
        return courseEntity;
    }

    // 2. DB Entity -> Domain 변환 (조회할 때)
    public Course toDomain(CourseJpaEntity entity) {
        List<Chapter> chapters = entity.getChapters().stream()
                .map(chEntity -> Chapter.withId(
                        chEntity.getId(),
                        chEntity.getTitle(),
                        chEntity.getVideoUrl(),
                        chEntity.getDurationSeconds(),
                        chEntity.getOrderNum()
                )).collect(Collectors.toList());

        return Course.withId(
                entity.getId(),
                entity.getCountryId(),
                entity.getManagerId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getFileUrl(),   // ✨ 누락되었던 파라미터 매핑
                entity.getStatus(),
                chapters
        );
    }
}