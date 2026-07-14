package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.application.result.CourseClassroomChapterResult;
import com.kidmily.algoga_server.course.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 강의실(CourseClassroomResult) 조립 책임을 CourseService에서 분리한 유틸리티.
 *
 * <p>이전 챕터 완료 여부에 따른 잠금 처리와 quizAvailable 계산 로직은 기존 CourseService 로직과 동일하다.
 * 상태와 외부 의존성이 없다.
 */
public final class CourseClassroomAssembler {

    private CourseClassroomAssembler() {
    }

    public static CourseClassroomResult assemble(
            Course course,
            Enrollment enrollment,
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        List<CourseClassroomChapterResult> chapterResults = new ArrayList<>();
        boolean previousChaptersCompleted = true;

        for (Chapter chapter : chapters) {
            LearningProgress progress = progressByChapterId.get(chapter.getId());
            boolean completed = progress != null && progress.isCompleted();
            boolean locked = !previousChaptersCompleted;

            chapterResults.add(new CourseClassroomChapterResult(
                    chapter.getId(),
                    chapter.getTitle(),
                    chapter.getDescription(),
                    locked ? null : chapter.getVideoUrl(),
                    chapter.getDurationSeconds(),
                    chapter.getChapterOrder(),
                    progress == null ? 0 : progress.getWatchedSeconds(),
                    progress == null ? 0 : progress.getProgressRate(),
                    completed,
                    locked
            ));

            previousChaptersCompleted = previousChaptersCompleted && completed;
        }

        boolean quizAvailable = !chapters.isEmpty()
                && chapterResults.stream().allMatch(CourseClassroomChapterResult::completed);

        return new CourseClassroomResult(
                course.getId(),
                course.getTitle(),
                enrollment.getAccessExpiresAt(),
                quizAvailable,
                chapterResults
        );
    }
}
