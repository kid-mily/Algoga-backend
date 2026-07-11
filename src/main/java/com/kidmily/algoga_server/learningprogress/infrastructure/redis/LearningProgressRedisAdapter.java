package com.kidmily.algoga_server.learningprogress.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.learningprogress.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningProgressRedisAdapter implements LearningProgressCachePort {

    private static final String KEY_PREFIX = "lms:progress:";
    private static final String DIRTY_SET_KEY = "lms:progress:dirty";
    private static final Duration PROGRESS_TTL = Duration.ofHours(6);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<LearningProgress> find(Long userId, Long courseId, Long chapterId) {
        String value = redisTemplate.opsForValue().get(key(userId, courseId, chapterId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, LearningProgressCachePayload.class).toDomain());
        } catch (JsonProcessingException exception) {
            log.warn("[LearningProgressRedis] Failed to read cached progress. userId={}, courseId={}, chapterId={}",
                    userId, courseId, chapterId, exception);
            return Optional.empty();
        }
    }

    @Override
    public void cacheClean(LearningProgress learningProgress) {
        write(learningProgress);
    }

    @Override
    public void cacheDirty(LearningProgress learningProgress) {
        String key = write(learningProgress);
        redisTemplate.opsForSet().add(DIRTY_SET_KEY, key);
    }

    @Override
    public List<LearningProgress> findDirtyProgresses() {
        Set<String> dirtyKeys = redisTemplate.opsForSet().members(DIRTY_SET_KEY);
        if (dirtyKeys == null || dirtyKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return dirtyKeys.stream()
                .map(this::read)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public void markFlushed(LearningProgress learningProgress) {
        String key = key(learningProgress.getUserId(), learningProgress.getCourseId(), learningProgress.getChapterId());
        Optional<LearningProgress> currentProgress = read(key);

        if (currentProgress.isPresent()
                && currentProgress.get().getWatchedSeconds() > learningProgress.getWatchedSeconds()) {
            log.debug("[LearningProgressRedis] Keep dirty progress because a newer value exists. key={}", key);
            return;
        }

        cacheClean(learningProgress);
        redisTemplate.opsForSet().remove(DIRTY_SET_KEY, key);
    }

    private String write(LearningProgress learningProgress) {
        String key = key(learningProgress.getUserId(), learningProgress.getCourseId(), learningProgress.getChapterId());
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(LearningProgressCachePayload.from(learningProgress)),
                    PROGRESS_TTL
            );
            return key;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize learning progress cache", exception);
        }
    }

    private Optional<LearningProgress> read(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            redisTemplate.opsForSet().remove(DIRTY_SET_KEY, key);
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, LearningProgressCachePayload.class).toDomain());
        } catch (JsonProcessingException exception) {
            log.warn("[LearningProgressRedis] Failed to read dirty progress. key={}", key, exception);
            return Optional.empty();
        }
    }

    private String key(Long userId, Long courseId, Long chapterId) {
        return KEY_PREFIX + userId + ":" + courseId + ":" + chapterId;
    }
}