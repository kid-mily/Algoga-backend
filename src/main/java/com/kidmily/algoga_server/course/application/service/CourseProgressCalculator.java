package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;

import java.util.List;

/**
 * 코스 진행률/완료 챕터 수/총 재생시간 등 순수 계산 로직을 CourseService에서 분리한 유틸리티.
 *
 * <p>상태와 외부 의존성이 없으며, 계산 결과는 기존 CourseService 로직과 동일하다.
 */
public final class CourseProgressCalculator {

    private CourseProgressCalculator() {
    }

    public static int calculateCompletedChapterCount(List<LearningProgress> progresses) {
        return (int) progresses.stream()
                .filter(LearningProgress::isCompleted)
                .count();
    }

    public static int calculateCourseProgressRate(
            List<Chapter> chapters,
            List<LearningProgress> progresses
    ) {
        if (chapters.isEmpty()) {
            return 0;
        }

        int totalProgressRate = 0;

        for (Chapter chapter : chapters) {
            int chapterProgressRate = progresses.stream()
                    .filter(progress -> progress.getChapterId().equals(chapter.getId()))
                    .map(LearningProgress::getProgressRate)
                    .findFirst()
                    .orElse(0);

            totalProgressRate += chapterProgressRate;
        }

        return Math.min(100, (int) Math.floor(totalProgressRate / (double) chapters.size()));
    }

    public static int calculateTotalDurationSeconds(List<Chapter> chapters) {
        return chapters.stream()
                .mapToInt(Chapter::getDurationSeconds)
                .sum();
    }
}
