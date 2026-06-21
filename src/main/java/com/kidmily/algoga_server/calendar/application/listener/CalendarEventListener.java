package com.kidmily.algoga_server.calendar.application.listener;

import com.kidmily.algoga_server.booking.domain.event.BookingCanceledEvent;
import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import com.kidmily.algoga_server.payment.domain.event.PackagePaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
import com.kidmily.algoga_server.refund.domain.event.RefundApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarEventListener {

    private final CalendarRepository calendarRepository;
    private final CalendarSchedulePolicy calendarSchedulePolicy;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePackagePaymentCompleted(PackagePaymentCompletedEvent event) {
        log.info("[CalendarEventListener] 패키지 결제 완료 커밋 확인 - userId: {}, accommodationId: {}",
                event.userId(), event.accommodationId());

        try {
            // 숙소 캘린더 저장
            Calendar tripCalendar = Calendar.create(
                    event.userId(),
                    event.accommodationId(),
                    event.checkInDate(),
                    CalendarType.TRIP
            );
            calendarRepository.save(tripCalendar);
            log.info("[CalendarEventListener] 숙소 캘린더 일정 저장 완료");

            // 항공권 캘린더 저장
            LocalDate departureDate = calendarSchedulePolicy.resolveFlightDepartureDate(event.bookingId());
            if (departureDate != null) {
                Calendar flightCalendar = Calendar.create(
                        event.userId(),
                        event.bookingId(),
                        departureDate,
                        CalendarType.FLIGHT
                );
                calendarRepository.save(flightCalendar);
                log.info("[CalendarEventListener] 항공권 캘린더 일정 저장 완료");
            }

        } catch (Exception e) {
            log.error("[CalendarEventListener] 패키지 결제 반영 중 캘린더 처리 실패! - userId: {}, error: {}",
                    event.userId(), e.getMessage(), e);
        }
    }


    // 단순 예약 취소 완료 시 일정 삭제
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookingCanceled(BookingCanceledEvent event) {
        log.info("[CalendarEventListener] 예약 취소 완료 커밋 확인 - userId: {}, accommodationId: {}",
                event.userId(), event.accommodationId());

        try {
            // 숙소 캘린더 삭제
            calendarRepository.deleteByUserIdAndReferenceIdAndType(
                    event.userId(),
                    event.accommodationId(),
                    CalendarType.TRIP
            );
            log.info("[CalendarEventListener] 여행 캘린더 일정 삭제 완료");

            // 항공권 삭제 추가
            calendarRepository.deleteByUserIdAndReferenceIdAndType(
                    event.userId(),
                    event.bookingId(),
                    CalendarType.FLIGHT
            );
            log.info("[CalendarEventListener] 항공권 캘린더 일정 삭제 완료");

        } catch (Exception e) {
            log.error("[CalendarEventListener] 예약 취소 중 캘린더 처리 실패! - userId: {}, error: {}",
                    event.userId(), e.getMessage(), e);
        }
    }

    // 강의 결제 완료 시 일정 추가
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 돈이 진짜 성공적으로 완전히 빠져나갔을 때만!
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLecturePaymentCompleted(PaymentCompletedEvent event) {
        if (event.courseId() == null) {
            return;
        }

        log.info("[CalendarEventListener] 강의 결제 완료 커밋 확인 - userId: {}, courseId: {}",
                event.userId(), event.courseId());

        try {
            Calendar calendar = Calendar.create(
                    event.userId(),
                    event.courseId(),
                    event.paidAt().toLocalDate(),
                    CalendarType.LECTURE
            );
            calendarRepository.save(calendar);
            log.info("[CalendarEventListener] 강의 캘린더 일정 저장 완료");
        } catch (Exception e) {
            log.error("[CalendarEventListener] 강의 결제 반영 중 캘린더 처리 실패! - userId: {}, courseId: {}, error: {}",
                    event.userId(), event.courseId(), e.getMessage(), e);
        }
    }

    // 환불 완료
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 성공 커밋 후 실행 장치
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 독자적인 새 트랜잭션 보장
    public void handleRefundApproved(RefundApprovedEvent event) {
        log.info("[CalendarEventListener] 환불 승인 도메인 이벤트 수신 - userId: {}, referenceId: {}, type: {}",
                event.userId(), event.referenceId(), event.type());

        try {
            CalendarType calendarType = CalendarType.valueOf(event.type());

            calendarRepository.deleteByUserIdAndReferenceIdAndType(
                    event.userId(),
                    event.referenceId(),
                    calendarType
            );
            log.info("[CalendarEventListener] 환불 반영으로 인한 캘린더 일정 정상 제거 완료");

            // 항공권 삭제 추가 (TRIP인 경우에만)
            if (calendarType == CalendarType.TRIP && event.bookingId() != null) {
                calendarRepository.deleteByUserIdAndReferenceIdAndType(
                        event.userId(),
                        event.bookingId(),
                        CalendarType.FLIGHT
                );
                log.info("[CalendarEventListener] 환불 반영으로 인한 항공권 캘린더 일정 정상 제거 완료");
            }

        } catch (IllegalArgumentException e) {
            log.error("[CalendarEventListener] 환불 타입 바인딩 실패 - type: {}", event.type());
        } catch (Exception e) {
            log.error("[CalendarEventListener] 환불 반영 중 캘린더 처리 실패! - userId: {}, error: {}",
                    event.userId(), e.getMessage(), e);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Calendar] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            calendarRepository.deleteAllByUserId(withdrawnUserId);
            log.info("[Calendar] 유저({}) 캘린더 데이터 삭제 완료", withdrawnUserId);
        } catch (Exception e) {
            log.error("[Calendar] 유저 탈퇴 캘린더 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}