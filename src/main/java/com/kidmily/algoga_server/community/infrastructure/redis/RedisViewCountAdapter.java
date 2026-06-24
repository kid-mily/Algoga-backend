package com.kidmily.algoga_server.community.infrastructure.redis;

import com.kidmily.algoga_server.community.domain.port.ViewCountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisViewCountAdapter implements ViewCountPort {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String VIEW_COUNT_KEY = "post:viewCount:";
    private static final String CHANGED_IDS_KEY = "changed:post:ids";

    @Override
    public void increment(Long postId) {
        redisTemplate.opsForValue().increment(VIEW_COUNT_KEY + postId);
        redisTemplate.opsForSet().add(CHANGED_IDS_KEY, String.valueOf(postId));
    }

    @Override
    public long getCurrentCount(Long postId) {
        Object value = redisTemplate.opsForValue().get(VIEW_COUNT_KEY + postId);
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    @Override
    public long getAndReset(Long postId) {
        Object value = redisTemplate.opsForValue().getAndSet(VIEW_COUNT_KEY + postId, "0");
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    @Override
    public Set<String> getChangedPostIds() {
        return redisTemplate.opsForSet().members(CHANGED_IDS_KEY);
    }

    @Override
    public void clearChangedPostIds() {
        redisTemplate.delete(CHANGED_IDS_KEY);
    }
}