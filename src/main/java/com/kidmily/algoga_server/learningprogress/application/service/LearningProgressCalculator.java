package com.kidmily.algoga_server.learningprogress.application.service;

import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 학습 진도 관련 순수 계산 유틸리티(다음 챕터 탐색, 코스 진행률, 퀴즈 응시 가능 여부).
 *
 * <p>계산 결과는 기존 LearningProgressService 로직과 동일하며 상태·외부 의존성이 없다.
 */
public final class LearningProgressCalculator {

    private LearningProgressCalculator() {
    }

    public static Long findNextChapterId(List<Chapter> chapters, Chapter currentChapter) {
        return chapters.stream()
                .filter(chapter -> chapter.getChapterOrder() > currentChapter.getChapterOrder())
                .min(Comparator.comparingInt(Chapter::getChapterOrder))
                .map(Chapter::getId)
                .orElse(null);
    }

    public static int calculateCourseProgressRate(
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        if (chapters.isEmpty()) {
            return 0;
        }

        int totalProgressRate = 0;

        for (Chapter chapter : chapters) {
            LearningProgress progress = progressByChapterId.get(chapter.getId());
            totalProgressRate += progress == null ? 0 : progress.getProgressRate();
        }

        return Math.min(100, (int) Math.floor(totalProgressRate / (double) chapters.size()));
    }

    public static boolean isQuizAvailable(
            List<Chapter> chapters,
            Map<Long, LearningProgress> progressByChapterId
    ) {
        if (chapters.isEmpty()) {
            return false;
        }

        return chapters.stream()
                .allMatch(chapter -> {
                    LearningProgress progress = progressByChapterId.get(chapter.getId());
                    return progress != null && progress.isCompleted();
                });
    }
}
