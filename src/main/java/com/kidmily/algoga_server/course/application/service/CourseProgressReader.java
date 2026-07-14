package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.learningprogress.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseProgressReader {

    private final LearningProgressRepository learningProgressRepository;
    private final LearningProgressCachePort learningProgressCachePort;

    public Map<Long, LearningProgress> loadProgressMapWithCache(
            Long userId,
            Long courseId,
            List<Chapter> chapters
    ) {
        Map<Long, LearningProgress> progressByChapterId = learningProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .stream()
                .collect(Collectors.toMap(
                        LearningProgress::getChapterId,
                        Function.identity(),
                        this::newerProgress,
                        LinkedHashMap::new
                ));

        for (Chapter chapter : chapters) {
            loadCachedProgress(userId, courseId, chapter.getId())
                    .ifPresent(cachedProgress -> progressByChapterId.merge(
                            chapter.getId(),
                            cachedProgress,
                            this::newerProgress
                    ));
        }

        return progressByChapterId;
    }

    public Optional<LearningProgress> loadProgressWithCache(
            Long userId,
            Long courseId,
            Long chapterId
    ) {
        Optional<LearningProgress> cachedProgress = loadCachedProgress(userId, courseId, chapterId);
        Optional<LearningProgress> dbProgress = learningProgressRepository.findByUserIdAndChapterId(userId, chapterId);

        if (cachedProgress.isPresent() && dbProgress.isPresent()) {
            return Optional.of(newerProgress(dbProgress.get(), cachedProgress.get()));
        }

        return cachedProgress.or(() -> dbProgress);
    }

    public List<LearningProgress> mergeProgressesWithCache(
            Long userId,
            Long courseId,
            List<Chapter> chapters,
            List<LearningProgress> dbProgresses
    ) {
        Map<Long, LearningProgress> progressByChapterId = dbProgresses.stream()
                .collect(Collectors.toMap(
                        LearningProgress::getChapterId,
                        Function.identity(),
                        this::newerProgress,
                        LinkedHashMap::new
                ));

        for (Chapter chapter : chapters) {
            loadCachedProgress(userId, courseId, chapter.getId())
                    .ifPresent(cachedProgress -> progressByChapterId.merge(
                            chapter.getId(),
                            cachedProgress,
                            this::newerProgress
                    ));
        }

        return new ArrayList<>(progressByChapterId.values());
    }

    private Optional<LearningProgress> loadCachedProgress(
            Long userId,
            Long courseId,
            Long chapterId
    ) {
        try {
            return learningProgressCachePort.find(userId, courseId, chapterId);
        } catch (RuntimeException exception) {
            log.warn("[CourseProgressReader] Failed to read cached learning progress. userId={}, courseId={}, chapterId={}",
                    userId, courseId, chapterId, exception);
            return Optional.empty();
        }
    }

    private LearningProgress newerProgress(
            LearningProgress first,
            LearningProgress second
    ) {
        if (second.getWatchedSeconds() > first.getWatchedSeconds()) {
            return second;
        }

        if (second.getWatchedSeconds() == first.getWatchedSeconds()
                && second.getProgressRate() > first.getProgressRate()) {
            return second;
        }

        return first;
    }
}
