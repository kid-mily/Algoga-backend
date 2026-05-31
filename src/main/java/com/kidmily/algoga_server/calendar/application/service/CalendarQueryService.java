package com.kidmily.algoga_server.calendar.application.service;

import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import com.kidmily.algoga_server.calendar.application.usecase.CalendarQueryUseCase;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import com.kidmily.algoga_server.calendar.presentation.api.response.CalendarResponse;
import com.kidmily.algoga_server.calendar.presentation.api.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarQueryService implements CalendarQueryUseCase {

    private final CalendarRepository calendarRepository;
    private final CalendarSchedulePolicy calendarSchedulePolicy;

    @Override
    public CalendarResponse getCalendar(Long userId, int year, int month) {
        log.info("[CalendarQueryService] 통합 캘린더 조회 요청 - userId: {}, year: {}, month: {}",
                userId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Calendar> calendars = calendarRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        List<ScheduleResponse> schedules = calendars.stream()
                .flatMap(calendar -> toScheduleResponses(calendar, userId).stream())
                .sorted((a, b) -> a.eventDate().compareTo(b.eventDate()))
                .toList();

        return new CalendarResponse(year, month, schedules);
    }

    private ScheduleResponse toScheduleResponse(Calendar calendar) {
        String title = resolveTitle(calendar);
        String dDayText = calculateDDay(calendar.getEventDate());

        return new ScheduleResponse(
                calendar.getCalendarId(),
                title,
                calendar.getType(),
                calendar.getEventDate(),
                dDayText
        );
    }

    private List<ScheduleResponse> toScheduleResponses(Calendar calendar, Long userId) {
        if (calendar.getType() == CalendarType.LECTURE) {
            String title = calendarSchedulePolicy.resolveLectureName(calendar.getReferenceId());
            LocalDate startDate = calendarSchedulePolicy.resolveLectureStartDate(calendar.getReferenceId(), userId);
            LocalDate endDate = calendarSchedulePolicy.resolveLectureEndDate(calendar.getReferenceId(), userId);

            if (startDate == null || endDate == null) {
                return List.of();
            }

            return List.of(
                    new ScheduleResponse(calendar.getCalendarId(), title,
                            CalendarType.LECTURE_START, startDate, calculateDDay(startDate)),
                    new ScheduleResponse(calendar.getCalendarId(), title,
                            CalendarType.LECTURE_END, endDate, calculateDDay(endDate))
            );
        }
        return List.of(toScheduleResponse(calendar));
    }

    private String resolveTitle(Calendar calendar) {
        if (calendar.getType() == CalendarType.TRIP) {
            return calendarSchedulePolicy.resolveAccommodationName(calendar.getReferenceId());
        }
        if (calendar.getType() == CalendarType.FLIGHT) {
            return calendarSchedulePolicy.resolveFlightName(calendar.getReferenceId());
        }
        return "D-day";
    }

    private String calculateDDay(LocalDate eventDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        if (days == 0) return "D-DAY";
        if (days > 0) return "D-" + days;
        return "D+" + Math.abs(days);
    }
}