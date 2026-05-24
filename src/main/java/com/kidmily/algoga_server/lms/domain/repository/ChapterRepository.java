package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository {

    Chapter save(Chapter chapter);

    List<Chapter> findByCourseId(Long courseId);

    Optional<Chapter> findByIdAndCourseId(Long chapterId, Long courseId);

    Optional<Chapter> updateBasicInfo(
            Long chapterId,
            Long courseId,
            String title,
            String videoUrl,
            int durationSeconds,
            int chapterOrder
    );

    boolean softDelete(Long chapterId, Long courseId);
}