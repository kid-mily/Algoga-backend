package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.infrastructure.pdf.ConfirmationPdfGenerator;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * 어드민 결제내역의 사용자명이 "스냅샷 우선 + 옛 결제건은 live 조회 fallback" 으로 동작하는지 검증.
 * DB·서버·로그인 없이 PaymentRepository/UserRepository 를 Mock 으로 대체해 조회 로직만 본다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceUserNameTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ConfirmationPdfGenerator confirmationPdfGenerator;
    @Mock private CourseRepository courseRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private MileageHistoryRepository mileageHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccommodationRepository accommodationRepository;

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    private final LocalDate from = LocalDate.of(2026, 6, 1);
    private final LocalDate to = LocalDate.of(2026, 6, 30);

    /** bookingId/courseId 를 null 로 둬 상품명 조회(course/booking repo)는 타지 않게 한 결제 1건 */
    private Payment paymentWithUserName(String userName) {
        return Payment.reconstitute(
                1L, null, null, 2L,
                PaymentType.LECTURE_ONLY, 50000, 0, null,
                PaymentStatus.SUCCESS, "LECTURE_1_2", "pid-1", "TOSSPAY",
                userName, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("스냅샷이 있으면 그 값을 쓰고 user 테이블을 조회하지 않는다")
    void 스냅샷_있으면_그대로_사용() {
        when(paymentRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(paymentWithUserName("김알고")));

        List<PaymentResponse> result = paymentQueryService.getAdminPayments(from, to);

        assertEquals("김알고", result.get(0).userName());
        verify(userRepository, never()).findById(anyLong());   // 스냅샷이 있으니 live 조회 안 함
    }

    @Test
    @DisplayName("스냅샷이 없으면(옛 결제) user 테이블을 live 조회해 채운다")
    void 스냅샷_없으면_live조회_fallback() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("고성민");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(paymentWithUserName(null)));

        List<PaymentResponse> result = paymentQueryService.getAdminPayments(from, to);

        assertEquals("고성민", result.get(0).userName());
    }

    @Test
    @DisplayName("스냅샷도 없고 user 도 삭제됐으면 사용자명은 null")
    void 스냅샷없고_유저삭제면_null() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(paymentRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(paymentWithUserName(null)));

        List<PaymentResponse> result = paymentQueryService.getAdminPayments(from, to);

        assertNull(result.get(0).userName());
    }
}
