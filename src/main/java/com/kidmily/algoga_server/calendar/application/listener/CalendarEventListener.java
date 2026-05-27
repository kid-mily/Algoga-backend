package com.kidmily.algoga_server.calendar.application.listener;

import com.kidmily.algoga_server.booking.domain.event.BookingCanceledEvent;
import com.kidmily.algoga_server.booking.domain.event.BookingCreatedEvent;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarEventListener {

    private final CalendarRepository calendarRepository;

    @EventListener
    @Transactional
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("[CalendarEventListener] 예약 생성 이벤트 수신 - userId: {}, packageId: {}",
                event.userId(), event.accommodationId());

        Calendar calendar = Calendar.create(
                event.userId(),
                event.accommodationId(),  // packageId → accommodationId
                event.checkInDate(),      // departureDate → checkInDate
                CalendarType.TRIP
        );
        calendarRepository.save(calendar);

        log.info("[CalendarEventListener] 캘린더 일정 저장 완료");
    }

    @EventListener
    @Transactional
    public void handleBookingCanceled(BookingCanceledEvent event) {
        log.info("[CalendarEventListener] 예약 취소 이벤트 수신 - accommodationId: {}", event.accommodationId());

        calendarRepository.deleteByReferenceId(event.accommodationId());

        log.info("[CalendarEventListener] 캘린더 일정 삭제 완료");
    }
}