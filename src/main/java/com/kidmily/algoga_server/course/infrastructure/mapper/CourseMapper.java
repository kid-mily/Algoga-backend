package com.kidmily.algoga_server.course.infrastructure.mapper;

import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.model.CourseFile;
import com.kidmily.algoga_server.course.infrastructure.persistence.entity.ChapterJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseFileJpaEntity;
import com.kidmily.algoga_server.course.infrastructure.persistence.entity.CourseJpaEntity;
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
                course.getMaxRewardMileage(),
                course.getThumbnailUrl(),
                course.getFileUrl(),
                toCourseFileEntities(course.getCourseFiles()),
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

        List<CourseFile> courseFiles = entity.getCourseFiles() == null
                ? List.of()
                : entity.getCourseFiles()
                .stream()
                .map(this::toCourseFileDomain)
                .toList();

        if (courseFiles.isEmpty() && entity.getFileUrl() != null && !entity.getFileUrl().isBlank()) {
            courseFiles = List.of(
                    CourseFile.withId(
                            null,
                            entity.getId(),
                            entity.getFileUrl(),
                            null,
                            1
                    )
            );
        }

        return Course.withId(
                entity.getId(),
                entity.getCountryId(),
                entity.getManagerId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getMaxRewardMileage(),
                entity.getThumbnailUrl(),
                entity.getFileUrl(),
                courseFiles,
                entity.getLevel(),
                entity.getStatus(),
                entity.isDeleted(),
                chapters
        );
    }

    public List<CourseFileJpaEntity> toCourseFileEntities(List<CourseFile> courseFiles) {
        if (courseFiles == null || courseFiles.isEmpty()) {
            return List.of();
        }

        return courseFiles.stream()
                .map(this::toCourseFileEntity)
                .toList();
    }

    private CourseFileJpaEntity toCourseFileEntity(CourseFile courseFile) {
        return new CourseFileJpaEntity(
                courseFile.getFileUrl(),
                courseFile.getOriginalFileName(),
                courseFile.getFileOrder()
        );
    }

    private CourseFile toCourseFileDomain(CourseFileJpaEntity entity) {
        return CourseFile.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getFileUrl(),
                entity.getOriginalFileName(),
                entity.getFileOrder()
        );
    }

    private Chapter toChapterDomain(ChapterJpaEntity entity) {
        return Chapter.withId(
                entity.getId(),
                entity.getCourseId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getVideoUrl(),
                entity.getDurationSeconds(),
                entity.getOrderNum(),
                entity.isDeleted()
        );
    }
}
