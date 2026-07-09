package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CountryRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.LectureToTripSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/*
 * LectureToTripStatsService.getSummary 단위 테스트
 * - 번들(같은 날 결제) 제외 / 완강 판정 / 후행 전환 / 완강 vs 미완강 배수 검증
 */
@ExtendWith(MockitoExtension.class)
class LectureToTripStatsServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseCompletionRepository courseCompletionRepository;
    @Mock private CountryRepository countryRepository;

    @InjectMocks
    private LectureToTripStatsService service;

    private static final LocalDateTime D = LocalDateTime.of(2026, 6, 1, 10, 0);

    private Payment lecture(Long userId) {
        return Payment.reconstitute(userId, null, 10L, userId, PaymentType.LECTURE_ONLY,
                100_000, 0, null, PaymentStatus.SUCCESS, "k" + userId, "p", "TOSSPAY", "u", D);
    }

    private Booking bookingAt(Long userId, LocalDateTime createdAt) {
        return Booking.reconstitute(userId, 1L, userId, BookingStatus.FULL_PAID, 1_000_000, 300_000, 700_000,
                "BK-" + userId, "{}", LocalDate.now().plusDays(30), LocalDate.now().plusDays(33), 3, createdAt, createdAt);
    }

    @Test
    @DisplayName("번들 제외 + 완강/전환 퍼널 + 완강 vs 미완강 배수를 정확히 집계한다")
    void 강의여행_전환_퍼널() {
        // 강의(course10 = country1) 구매자 5명
        when(paymentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                lecture(1L), lecture(2L), lecture(3L), lecture(4L), lecture(5L)));

        Course course = mock(Course.class);
        when(course.getId()).thenReturn(10L);
        when(course.getCountryId()).thenReturn(1L);
        when(courseRepository.findBasicByIdIn(anyList())).thenReturn(List.of(course));

        Accommodation acc = Accommodation.reconstitute(1L, 1L, "호텔", "주소", "img", 400_000, 3, "설명");
        when(accommodationRepository.findById(1L)).thenReturn(java.util.Optional.of(acc));

        // user1: 후행 예약(D+5), user2: 같은날 예약(번들→제외), user4: 후행 예약(D+3), user3/5: 예약 없음
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(bookingAt(1L, D.plusDays(5))));
        when(bookingRepository.findByUserId(2L)).thenReturn(List.of(bookingAt(2L, D)));            // 번들
        when(bookingRepository.findByUserId(3L)).thenReturn(List.of());
        when(bookingRepository.findByUserId(4L)).thenReturn(List.of(bookingAt(4L, D.plusDays(3))));
        when(bookingRepository.findByUserId(5L)).thenReturn(List.of());

        // 완강: user1, user3 (user4/5 미완강). user2는 번들이라 완강 조회 안 됨
        when(courseCompletionRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);
        when(courseCompletionRepository.existsByUserIdAndCourseId(3L, 10L)).thenReturn(true);
        when(courseCompletionRepository.existsByUserIdAndCourseId(4L, 10L)).thenReturn(false);
        when(courseCompletionRepository.existsByUserIdAndCourseId(5L, 10L)).thenReturn(false);

        // when
        LectureToTripSummaryResponse res = service.getSummary(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 1));

        // then: user2(번들) 제외 → 구매자 4
        assertEquals(4, res.lectureBuyers());
        assertEquals(2, res.completedCount());            // user1, user3
        assertEquals(50.0, res.completionRate());         // 2/4
        assertEquals(1, res.convertedCount());            // 완강&전환 = user1
        assertEquals(50.0, res.completedConversionRate()); // 1/2 완강자 중 전환
        assertEquals(50.0, res.notCompletedConversionRate()); // user4 전환 / 미완강 2
        assertEquals(1.0, res.vsNonCompletedMultiple());   // 50/50
    }
}
