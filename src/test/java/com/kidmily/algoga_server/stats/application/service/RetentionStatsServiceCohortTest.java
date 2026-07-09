package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.CohortResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/*
 * RetentionStatsService.getCohort 단위 테스트
 * - 가입월 코호트의 누적 구매 전환율(히트맵) + 누적매출 집계 검증
 * - 코호트월을 충분히 과거(2020)로 잡아 관측창(observable)=12로 고정 → null 없이 전 컬럼 채워짐
 */
@ExtendWith(MockitoExtension.class)
class RetentionStatsServiceCohortTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RetentionStatsService service;

    private UserRepository.SignupInfo signup(Long id, LocalDateTime at) {
        return new UserRepository.SignupInfo() {
            @Override public Long getUserId() { return id; }
            @Override public LocalDateTime getCreatedAt() { return at; }
        };
    }

    private Payment payment(Long userId, int amount, LocalDateTime at) {
        return Payment.reconstitute(userId, null, null, userId, PaymentType.FULL,
                amount, 0, null, PaymentStatus.SUCCESS, "u" + userId, "p", "TOSSPAY", "u", at);
    }

    @Test
    @DisplayName("가입월 코호트의 누적 구매 전환율과 누적매출을 정확히 집계한다")
    void 코호트_누적전환율_누적매출() {
        LocalDateTime jan = LocalDateTime.of(2020, 1, 15, 10, 0); // 코호트 2020-01

        // 2020-01 코호트 유저 4명 (id 1~4)
        when(userRepository.findActiveSignupInfos()).thenReturn(List.of(
                signup(1L, jan), signup(2L, jan), signup(3L, jan), signup(4L, jan)));

        // 결제(SUCCESS):
        //  user1 → M0(2020-01) 100k
        //  user2 → M1(2020-02) 200k
        //  user3 → M0(2020-01) 50k, M2(2020-03) 300k
        //  user4 → 결제 없음
        when(paymentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
                payment(1L, 100_000, LocalDateTime.of(2020, 1, 20, 9, 0)),
                payment(2L, 200_000, LocalDateTime.of(2020, 2, 10, 9, 0)),
                payment(3L, 50_000, LocalDateTime.of(2020, 1, 25, 9, 0)),
                payment(3L, 300_000, LocalDateTime.of(2020, 3, 5, 9, 0))));

        CohortResponse res = service.getCohort(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));

        assertEquals(12, res.maxMonths());
        assertEquals(1, res.cohorts().size());

        CohortResponse.CohortRow row = res.cohorts().get(0);
        assertEquals("2020-01", row.cohortMonth());
        assertEquals(4, row.cohortSize());

        // 누적 전환율: 첫결제 M0=user1,user3(2명) → 50%, M1=+user2 → 75%, 이후 유지(user4는 영원히 미전환)
        assertEquals(50.0, row.retentionRate().get(0));  // M0: 2/4
        assertEquals(75.0, row.retentionRate().get(1));  // M1: 3/4
        assertEquals(75.0, row.retentionRate().get(2));  // M2: 여전히 3/4 (user3 재구매지만 신규 전환 아님)
        assertEquals(75.0, row.retentionRate().get(11)); // 관측창 12개월 전부 채워짐 (null 없음)

        // 누적매출: M0=150k(u1+u3), M1=+200k=350k, M2=+300k=650k, 이후 유지
        assertEquals(150_000L, row.cumulativeRevenue().get(0));
        assertEquals(350_000L, row.cumulativeRevenue().get(1));
        assertEquals(650_000L, row.cumulativeRevenue().get(2));
        assertEquals(650_000L, row.cumulativeRevenue().get(11));
    }

    @Test
    @DisplayName("해당 기간에 가입 코호트가 없으면 빈 목록을 반환한다")
    void 코호트_없음() {
        when(userRepository.findActiveSignupInfos()).thenReturn(List.of());

        CohortResponse res = service.getCohort(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));

        assertEquals(12, res.maxMonths());
        assertTrue(res.cohorts().isEmpty());
    }
}
