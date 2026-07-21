package com.kidmily.algoga_server.learningprogress.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis가 실제로 떠 있지 않은 환경이라 Lua 스크립트의 "watchedSeconds가 줄어들면 쓰기를 건너뛴다"는
 * 동작 자체는 여기서 검증하지 못한다(실제 Redis 붙여서 통합 테스트로 확인 필요).
 * 이 테스트는 어댑터가 더 이상 무조건 덮어쓰는 opsForValue().set()이 아니라,
 * compare-and-set용 Lua 스크립트를 통해서, 그리고 현재 watchedSeconds를 비교 기준값으로
 * 실어서 쓰기를 시도한다는 것만 고정한다(회귀 방지).
 */
@ExtendWith(MockitoExtension.class)
class LearningProgressRedisAdapterTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private SetOperations<String, String> setOperations;

    private LearningProgressRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LearningProgressRedisAdapter(redisTemplate, new ObjectMapper());
    }

    @Test
    void writesThroughAtomicCompareAndSetScriptInsteadOfBlindSet() {
        LearningProgress progress = LearningProgress.withId(1L, 10L, 20L, 30L, 150, 50, false);

        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(1L);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        adapter.cacheDirty(progress);

        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> watchedSecondsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> ttlCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisTemplate).execute(
                any(RedisScript.class),
                keysCaptor.capture(),
                payloadCaptor.capture(),
                watchedSecondsCaptor.capture(),
                ttlCaptor.capture()
        );

        assertEquals(List.of("lms:progress:10:20:30"), keysCaptor.getValue());
        // 비교 기준값으로 넘기는 watchedSeconds — Lua가 현재 캐시값과 비교할 때 쓰는 값
        assertEquals("150", watchedSecondsCaptor.getValue());
        assertTrue(payloadCaptor.getValue().contains("\"watchedSeconds\":150"));
        assertEquals(String.valueOf(java.time.Duration.ofHours(6).toSeconds()), ttlCaptor.getValue());
    }
}
