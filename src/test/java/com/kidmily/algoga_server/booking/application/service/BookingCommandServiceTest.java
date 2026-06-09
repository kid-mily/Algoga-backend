package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * BookingCommandService 단위 테스트
 * - 예약 취소 성공 / 없는 예약 취소 예외 / cancel() 호출 여부 검증
 */
@ExtendWith(MockitoExtension.class)
class BookingCommandServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingCommandService bookingCommandService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(bookingCommandService);
    }

    @Test
    @DisplayName("예약 취소 시 cancel() 호출 확인")
    void 예약_취소_성공() {
        // given
        Booking booking = mock(Booking.class);
        when(booking.getUserId()).thenReturn(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when
        bookingCommandService.cancel(1L, 1L);

        // then : 도메인 cancel() 이 호출되었는지 검증
        verify(booking).cancel();
    }

    @Test
    @DisplayName("존재하지 않는 예약 취소 시 예외 발생")
    void 없는_예약_취소_시_예외_발생() {
        // given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () ->
                bookingCommandService.cancel(999L, 1L));
    }

    @Test
    @DisplayName("취소 후 상태가 CANCEL_REQUESTED 로 변경 확인")
    void 취소_후_상태_변경_확인() {
        // given
        Booking booking = Booking.reconstitute(
                1L, 1L, 1L, BookingStatus.PENDING,
                100000, 50000, 50000,
                "BK-001", null, null, null, 3, null, null
        );
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when
        bookingCommandService.cancel(1L, 1L);

        // then
        assertEquals(BookingStatus.CANCEL_REQUESTED, booking.getStatus());
    }
}