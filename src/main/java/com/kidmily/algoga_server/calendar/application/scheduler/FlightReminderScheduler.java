package com.kidmily.algoga_server.calendar.application.scheduler;

import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import com.kidmily.algoga_server.calendar.application.service.FlightReminderMailService;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightReminderScheduler {

    private final CalendarRepository calendarRepository;
    private final FlightReminderMailService flightReminderMailService;
    private final CalendarSchedulePolicy calendarSchedulePolicy;

    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행
    public void sendFlightReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        log.info("[FlightReminderScheduler] 항공권 리마인더 스케줄러 실행 - 현재: {}", now);

        List<Calendar> flightCalendars = calendarRepository.findByType(CalendarType.FLIGHT);

        for (Calendar calendar : flightCalendars) {
            try {
                // 이미 발송된 경우 스킵
                if (Boolean.TRUE.equals(calendar.getIsDDayAlertSent())) {
                    continue;
                }

                LocalDateTime departureDateTime = calendarSchedulePolicy.resolveDepartureDateTime(calendar.getReferenceId());

                if (departureDateTime == null) {
                    log.warn("[FlightReminderScheduler] 출발 시각 없음 - calendarId: {}", calendar.getCalendarId());
                    continue;
                }

                if (departureDateTime.isAfter(now) && departureDateTime.isBefore(in24Hours)) {
                    log.info("[FlightReminderScheduler] 리마인더 발송 대상 - userId: {}, departureDateTime: {}",
                            calendar.getUserId(), departureDateTime);

                    flightReminderMailService.sendFlightReminder(
                            calendar.getUserId(),
                            calendar.getReferenceId(),
                            departureDateTime.toLocalDate()
                    );

                    // 발송 완료 처리
                    calendar.markDDayAlertSent();
                    calendarRepository.update(calendar);
                    log.info("[FlightReminderScheduler] 발송 완료 처리 - calendarId: {}", calendar.getCalendarId());
                }
            } catch (Exception e) {
                log.error("[FlightReminderScheduler] 리마인더 처리 실패 - calendarId: {}, error: {}",
                        calendar.getCalendarId(), e.getMessage());
            }
        }
        log.info("[FlightReminderScheduler] 항공권 리마인더 발송 완료");
    }
}