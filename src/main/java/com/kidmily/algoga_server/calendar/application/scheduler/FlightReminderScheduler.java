package com.kidmily.algoga_server.calendar.application.scheduler;

import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import com.kidmily.algoga_server.calendar.application.service.FlightReminderMailService;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightReminderScheduler {

    private final CalendarRepository calendarRepository;
    private final FlightReminderMailService flightReminderMailService;
    private final CalendarSchedulePolicy calendarSchedulePolicy;

    @Scheduled(cron = "0 0 * * * *")   // 매 정시 (운영)
    public void sendFlightReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        log.info("[FlightReminderScheduler] 항공권 리마인더 스케줄러 실행 - 현재: {}", now);

        // 오늘~내일 출발 + 미발송 건만 조회 (23시 이후 출발 누락 방지)
        LocalDate today = now.toLocalDate();
        List<Calendar> flightCalendars =
                calendarRepository.findFlightRemindTargets(today, today.plusDays(1));

        for (Calendar calendar : flightCalendars) {
            try {
                LocalDateTime departureDateTime =
                        calendarSchedulePolicy.resolveDepartureDateTime(calendar.getReferenceId());

                if (departureDateTime == null) {
                    continue;
                }

                if (departureDateTime.isAfter(now) && departureDateTime.isBefore(in24Hours)) {
                    flightReminderMailService.sendFlightReminder(
                            calendar.getUserId(),
                            calendar.getReferenceId(),
                            departureDateTime.toLocalDate()
                    );

                    calendar.markDDayAlertSent();
                    calendarRepository.update(calendar);
                }
            } catch (Exception e) {
                log.error("[FlightReminderScheduler] 리마인더 처리 실패 - calendarId: {}", calendar.getCalendarId());
            }
        }
        log.info("[FlightReminderScheduler] 항공권 리마인더 발송 완료");
    }
}