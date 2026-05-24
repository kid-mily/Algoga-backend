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

    public Course toDomain(CourseJpaEntity entity) {
        List<Chapter> chapters = entity.getChapters().stream()
                .map(chEntity -> Chapter.withId(
                        chEntity.getId(),
                        chEntity.getTitle(),
                        chEntity.getVideoUrl(),
                        chEntity.getDurationSeconds(),
                        chEntity.getOrderNum()
                ))
                .collect(Collectors.toList());

        return Course.withId(
                entity.getId(),
                entity.getCountryId(),
                entity.getManagerId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getFileUrl(),
                entity.getStatus(),
                entity.isDeleted(),
                chapters
        );
    }
}