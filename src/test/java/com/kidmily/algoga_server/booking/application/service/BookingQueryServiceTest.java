package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * BookingQueryService.getMyBookingsByCountry 단위 테스트
 * - booking엔 countryId가 없어 accommodation → country 매핑으로 나라별 필터링하는 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class BookingQueryServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;

    @InjectMocks
    private BookingQueryService bookingQueryService;

    private Booking bookingAt(Long id, Long accommodationId, BookingStatus status) {
        return Booking.reconstitute(
                id, accommodationId, 1L, status,
                1_200_000, 360_000, 840_000,
                "BK-20260706-0000" + id, "{}", null, null,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 4), 3, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Accommodation accommodation(Long id, Long countryId) {
        return Accommodation.reconstitute(id, countryId, "호텔" + id, "주소", "img", 400_000, 3, "설명");
    }

    @Test
    @DisplayName("countryId 지정 시 해당 나라 숙소의 예약만 반환한다")
    void 나라별_예약만_필터링() {
        // given: 유저는 3건 예약 (숙소 1,2 = countryId 1 / 숙소 9 = 다른 나라)
        Long userId = 1L;
        Long countryId = 1L;
        when(accommodationRepository.findByCountryId(countryId))
                .thenReturn(List.of(accommodation(1L, countryId), accommodation(2L, countryId)));
        when(bookingRepository.findByUserId(userId)).thenReturn(List.of(
                bookingAt(1L, 1L, BookingStatus.FULL_PAID),
                bookingAt(2L, 2L, BookingStatus.DEPOSIT_PAID),
                bookingAt(3L, 9L, BookingStatus.PENDING) // 다른 나라 숙소 → 제외돼야 함
        ));

        // when
        List<BookingResponse> result = bookingQueryService.getMyBookingsByCountry(userId, countryId);

        // then: 다른 나라 예약(숙소9)은 빠지고 2건만
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.accommodationId() == 1L || r.accommodationId() == 2L));
    }

    @Test
    @DisplayName("예약 상태(status)가 응답에 그대로 담겨 FE 버튼 게이팅에 쓸 수 있다")
    void 예약_상태_전달() {
        // given
        Long userId = 1L;
        Long countryId = 1L;
        when(accommodationRepository.findByCountryId(countryId))
                .thenReturn(List.of(accommodation(1L, countryId)));
        when(bookingRepository.findByUserId(userId))
                .thenReturn(List.of(bookingAt(1L, 1L, BookingStatus.DEPOSIT_PAID)));

        // when
        List<BookingResponse> result = bookingQueryService.getMyBookingsByCountry(userId, countryId);

        // then: DEPOSIT_PAID → FE는 "잔금 결제하기" 버튼 판단
        assertEquals(1, result.size());
        assertEquals(BookingStatus.DEPOSIT_PAID, result.get(0).status());
    }

    @Test
    @DisplayName("해당 나라에 숙소가 없으면 예약 조회 없이 빈 리스트를 반환한다")
    void 나라에_숙소_없으면_빈_리스트() {
        // given
        Long userId = 1L;
        Long countryId = 999L;
        when(accommodationRepository.findByCountryId(countryId)).thenReturn(List.of());

        // when
        List<BookingResponse> result = bookingQueryService.getMyBookingsByCountry(userId, countryId);

        // then: 숙소가 없으니 booking 조회 자체를 스킵 (불필요한 쿼리 방지)
        assertTrue(result.isEmpty());
        verify(bookingRepository, never()).findByUserId(anyLong());
    }
}
