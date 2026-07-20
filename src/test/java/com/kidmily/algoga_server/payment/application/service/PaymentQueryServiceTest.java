package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.pdf.ConfirmationPdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * PaymentQueryService 단위 테스트
 * - Spring Context 없이 Mockito 로 의존성을 가짜 객체로 대체
 * - 강의 결제 금액 계산 로직만 집중 테스트
 */
@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ConfirmationPdfGenerator confirmationPdfGenerator;
    @Mock private CourseRepository courseRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private MileageHistoryRepository mileageHistoryRepository;

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @BeforeEach
    @DisplayName("Mock 객체 주입 확인")
    void setUp() {
        assertNotNull(paymentQueryService);
    }

    @Test
    @DisplayName("쿠폰/마일리지 없으면 강의 원가 그대로 반환")
    void 쿠폰_마일리지_없으면_원가_반환() {
        // given
        Course course = mock(Course.class);
        when(course.getPrice()).thenReturn(10000);
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        // when
        int result = paymentQueryService.calculateLectureAmount(1L, 0, null, 1L);

        // then
        assertEquals(10000, result);
    }

    @Test
    @DisplayName("퍼센트 쿠폰 20% 적용 시 8000원 반환")
    void 퍼센트_쿠폰_적용_시_할인된_금액_반환() {
        // given
        Course course = mock(Course.class);
        when(course.getPrice()).thenReturn(10000);
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getDiscountType()).thenReturn("PERCENT");
        when(coupon.getDiscountValue()).thenReturn(20);
        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        // when
        int result = paymentQueryService.calculateLectureAmount(1L, 0, 1L, 1L);

        // then
        assertEquals(8000, result); // 10000 - 20% = 8000
    }

    @Test
    @DisplayName("마일리지 잔액 부족 시 BusinessException 발생")
    void 마일리지_잔액_부족_시_예외_발생() {
        // given : 잔액 500 인데 1000 사용 시도
        Course course = mock(Course.class);
        when(course.getPrice()).thenReturn(10000);
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        MileageHistory history = mock(MileageHistory.class);
        when(history.getType()).thenReturn("EARN");
        when(history.getAmount()).thenReturn(500);
        when(history.isAvailableAt(any())).thenReturn(true);
        when(mileageHistoryRepository.findByUserId(1L)).thenReturn(List.of(history));

        // when & then
        assertThrows(BusinessException.class, () ->
                paymentQueryService.calculateLectureAmount(1L, 1000, null, 1L));
    }
}