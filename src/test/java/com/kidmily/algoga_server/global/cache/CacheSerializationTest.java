package com.kidmily.algoga_server.global.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Redis 캐시 값 직렬화 회귀 테스트.
 * <p>
 * 과거 {@code DefaultTyping.NON_FINAL} 설정 때문에 <b>캐시 쓰기는 되고 읽기만 항상 실패</b>해
 * 캐시가 걸린 조회 API가 두 번째 요청부터 500 을 내던 버그가 있었다.
 * (응답 DTO 가 record → final, {@code Stream.toList()} 결과도 final 불변 List 라
 *  타입 정보가 안 붙는데 읽을 때는 Object 기준으로 타입 정보를 요구했기 때문)
 * <p>
 * {@link CacheManagerConfig} 와 동일한 ObjectMapper 구성으로 저장→복원이 되는지 고정한다.
 */
class CacheSerializationTest {

    /** CacheManagerConfig 와 동일한 직렬화 설정 */
    private GenericJackson2JsonRedisSerializer serializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    private BookingResponse sample() {
        return new BookingResponse(
                1L, 1L, 1L, BookingStatus.DEPOSIT_PAID,
                770_000, 231_000, 539_000, "BK-20260719-12345",
                "{\"airline\":\"대한항공\"}", null, null,
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 19), 4, true,
                LocalDateTime.of(2026, 7, 19, 10, 0));
    }

    @Test
    @DisplayName("getMyBookings 반환값(record 리스트)이 캐시에 저장·복원된다")
    void record_리스트_왕복() {
        // 실제 서비스와 동일하게 Stream.toList() 로 만든 불변 리스트
        List<BookingResponse> value = List.of(sample()).stream().map(b -> b).toList();
        GenericJackson2JsonRedisSerializer serializer = serializer();

        Object restored = serializer.deserialize(serializer.serialize(value));

        assertNotNull(restored);
        List<?> list = assertInstanceOf(List.class, restored);
        assertEquals(1, list.size());
        // 원소가 Map 이 아니라 원래 record 타입으로 복원돼야 한다
        BookingResponse first = assertInstanceOf(BookingResponse.class, list.get(0));
        assertEquals("BK-20260719-12345", first.bookingNumber());
        assertEquals(BookingStatus.DEPOSIT_PAID, first.status());
        assertEquals(LocalDate.of(2026, 8, 15), first.checkInDate());
        assertEquals(231_000, first.depositPrice());
        assertEquals(true, first.installmentAllowed());
    }

    @Test
    @DisplayName("빈 리스트도 캐시에 저장·복원된다 (예약 없는 유저)")
    void 빈리스트_왕복() {
        List<BookingResponse> value = List.<BookingResponse>of().stream().toList();
        GenericJackson2JsonRedisSerializer serializer = serializer();

        Object restored = serializer.deserialize(serializer.serialize(value));

        List<?> list = assertInstanceOf(List.class, restored);
        assertEquals(0, list.size());
    }
}
