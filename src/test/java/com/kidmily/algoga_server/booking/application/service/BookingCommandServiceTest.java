package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingSource;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.lms.domain.model.CourseCompletion;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * BookingCommandService 단위 테스트
 * - 예약 취소 성공 / 없는 예약 취소 예외 / cancel() 호출 여부 검증
 */
@ExtendWith(MockitoExtension.class)
class BookingCommandServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseCompletionRepository courseCompletionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingCommandService bookingCommandService;

    private CreateBookingCommand command(BookingSource source) {
        return new CreateBookingCommand(1L, 1L, "{}", null, null, 300_000,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), source);
    }

    private Accommodation accommodationMock() {
        Accommodation acc = mock(Accommodation.class);
        when(acc.getPricePerNight()).thenReturn(100_000);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        return acc;
    }

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(bookingCommandService);
    }

    @Test
    @DisplayName("예약 취소 시 리포지토리로 CANCEL_REQUESTED 상태 변경을 요청한다")
    void 예약_취소_성공() {
        // given
        Booking booking = mock(Booking.class);
        when(booking.getUserId()).thenReturn(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when
        bookingCommandService.cancel(1L, 1L);

        // then : 현재 구현은 도메인 cancel()이 아니라 리포지토리로 상태를 업데이트한다
        verify(bookingRepository).updateStatus(1L, BookingStatus.CANCEL_REQUESTED);
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
                "BK-001", null, null, null, null, null, 3, false, null, null
        );
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // when
        bookingCommandService.cancel(1L, 1L);

        // then : 상태 변경은 리포지토리(updateStatus)를 통해 CANCEL_REQUESTED 로 요청된다
        verify(bookingRepository).updateStatus(1L, BookingStatus.CANCEL_REQUESTED);
    }

    @Test
    @DisplayName("LOUNGE 예약은 완강 체크 없이 생성되고 분할 결제가 허용된다(installmentAllowed=true)")
    void 라운지_예약_분할허용() {
        accommodationMock();

        bookingCommandService.handle(command(BookingSource.LOUNGE));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertTrue(captor.getValue().isInstallmentAllowed());
        // 라운지 경로는 완강 조회를 하지 않는다
        verifyNoInteractions(courseRepository, courseCompletionRepository);
    }

    @Test
    @DisplayName("COMPLETION 예약은 완강 시 생성되고 일시불만 허용된다(installmentAllowed=false)")
    void 완강후_예약_일시불고정() {
        Accommodation acc = accommodationMock();
        when(acc.getCountryId()).thenReturn(1L);

        Course course = mock(Course.class);
        when(course.getId()).thenReturn(10L);
        when(courseRepository.findPublishedByCountryId(1L)).thenReturn(List.of(course));
        when(courseCompletionRepository.findByUserIdAndCourseIdIn(eq(1L), anyList()))
                .thenReturn(List.of(mock(CourseCompletion.class)));

        bookingCommandService.handle(command(BookingSource.COMPLETION));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertFalse(captor.getValue().isInstallmentAllowed());
    }

    @Test
    @DisplayName("COMPLETION 예약인데 완강 안 했으면 LECTURE_NOT_COMPLETED 예외가 발생하고 저장되지 않는다")
    void 미완강_예약_차단() {
        Accommodation acc = mock(Accommodation.class);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(acc.getCountryId()).thenReturn(1L);

        Course course = mock(Course.class);
        when(course.getId()).thenReturn(10L);
        when(courseRepository.findPublishedByCountryId(1L)).thenReturn(List.of(course));
        when(courseCompletionRepository.findByUserIdAndCourseIdIn(eq(1L), anyList()))
                .thenReturn(List.of());

        assertThrows(BusinessException.class, () ->
                bookingCommandService.handle(command(BookingSource.COMPLETION)));
        verify(bookingRepository, never()).save(any());
    }
}