package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.event.LecturePaymentCompletedEvent;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * PaymentTransactionService.saveLecturePayment() 검증.
 * 강의 결제 성공 시 결제수단/사용자명 스냅샷이 저장되고, 수강권 부여용 이벤트가 발행되는지 확인.
 */
@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CourseRepository courseRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private MileageHistoryRepository mileageHistoryRepository;

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @Test
    @DisplayName("강의 결제 성공 시 결제수단·사용자명 스냅샷 저장 + 수강권 이벤트 발행")
    void 강의결제_성공_스냅샷_저장_및_수강권이벤트() {
        // given
        CreateLecturePaymentCommand command =
                new CreateLecturePaymentCommand(5L, 2L, 50000, 0, null, "pid-1");

        when(courseRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(mock(Course.class)));
        when(paymentRepository.findByIdempotencyKey("LECTURE_5_2")).thenReturn(Optional.empty());

        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        when(user.getName()).thenReturn("고성민");
        when(user.getEmail()).thenReturn("test@algoga.kr");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        paymentTransactionService.saveLecturePayment(command, "PAID", 50000, "TOSSPAY");

        // then : 저장된 Payment 에 결제수단/사용자명 스냅샷이 박혔는지
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals("TOSSPAY", saved.getPaymentMethod());
        assertEquals("고성민", saved.getUserName());
        assertEquals(PaymentStatus.SUCCESS, saved.getStatus());

        // 수강권 부여용 이벤트 발행
        verify(eventPublisher).publishEvent(any(LecturePaymentCompletedEvent.class));
    }
}
