package com.kidmily.algoga_server.lms.infrastructure.mapper;

import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.ChapterJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseMapper {

    public CourseJpaEntity toEntity(Course course) {
        return new CourseJpaEntity(
                course.getCountryId(),
                course.getManagerId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                course.getLevel(),
                course.getStatus()
        );
    }

    public Course toDomain(CourseJpaEntity entity) {
        List<Chapter> chapters = entity.getChapters() == null
                ? List.of()
                : entity.getChapters()
                .stream()
                .filter(chapter -> !chapter.isDeleted())
                .map(this::toChapterDomain)
                .toList();

        return Course.withId(
                entity.getId(),
                entity.getCountryId(),
                entity.getManagerId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getThumbnailUrl(),
                entity.getFileUrl(),
                entity.getLevel(),
                entity.getStatus(),
                entity.isDeleted(),
                chapters
        );
    }

    private Chapter toChapterDomain(ChapterJpaEntity entity) {
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