package com.kidmily.algoga_server.booking.application.scheduler;

import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 출발일이 지났는데 결제가 전혀 없는(PENDING) 예약을 EXPIRED 로 전환하는 배치.
 * <p>
 * <b>대상은 PENDING 만.</b> DEPOSIT_PAID(예약금이 이미 결제된 건)는 실제 돈이 오간 상태라
 * 자동 만료시키면 환불 없이 예약을 없애는 꼴이 되므로 대상에서 제외한다(환불/CS 흐름에서 처리).
 * <p>
 * PENDING·EXPIRED 는 모두 내 예약 목록({@code getMyBookings})에서 숨겨지므로
 * 이 전환으로 사용자 목록 캐시(MY_BOOKINGS)가 달라지지 않는다 → 별도 캐시 무효화 불필요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;

    // 매일 자정 직후(00:10) 실행 — 전날까지 출발일이 지난 미결제 예약 정리
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void expireUnpaidPastBookings() {
        LocalDate today = LocalDate.now();
        List<Booking> targets =
                bookingRepository.findByStatusAndCheckInDateBefore(BookingStatus.PENDING, today);

        if (targets.isEmpty()) {
            return;
        }

        targets.forEach(booking -> bookingRepository.updateStatus(booking.getId(), BookingStatus.EXPIRED));

        log.info("[배치 작업] 출발일 지난 미결제(PENDING) 예약 {}건 EXPIRED 전환 완료", targets.size());
    }
}
