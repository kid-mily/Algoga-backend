package com.kidmily.algoga_server.service.calendar.service;

import com.kidmily.algoga_server.calendar.application.policy.CalendarSchedulePolicy;
import com.kidmily.algoga_server.calendar.application.service.CalendarQueryService;
import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import com.kidmily.algoga_server.calendar.presentation.api.response.CalendarResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarQueryService 테스트")
class CalendarQueryServiceTest {

    @InjectMocks
    private CalendarQueryService calendarQueryService;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private CalendarSchedulePolicy calendarSchedulePolicy;

    @Test
    @DisplayName("해당 월에 일정이 있으면 일정 목록을 반환한다.")
    void getCalendar_success() {
        // given
        Long userId = 1L;
        int year = 2026;
        int month = 6;

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Calendar tripCalendar = Calendar.reconstitute(
                1L, userId, 1L,
                LocalDate.of(2026, 6, 14),
                CalendarType.TRIP,
                false,
                LocalDateTime.now()
        );

        given(calendarRepository.findByUserIdAndDateRange(userId, startDate, endDate))
                .willReturn(List.of(tripCalendar));
        given(calendarSchedulePolicy.resolveAccommodationName(1L))
                .willReturn("도쿄 호텔");

        // when
        CalendarResponse response = calendarQueryService.getCalendar(userId, year, month);

        // then
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.schedules()).hasSize(1);
        assertThat(response.schedules().get(0).title()).isEqualTo("도쿄 호텔");
        assertThat(response.schedules().get(0).type()).isEqualTo(CalendarType.TRIP);
    }

    @Test
    @DisplayName("해당 월에 일정이 없으면 빈 리스트를 반환한다.")
    void getCalendar_empty() {
        // given
        Long userId = 1L;
        int year = 2026;
        int month = 6;

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        given(calendarRepository.findByUserIdAndDateRange(userId, startDate, endDate))
                .willReturn(List.of());

        // when
        CalendarResponse response = calendarQueryService.getCalendar(userId, year, month);

        // then
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.schedules()).isEmpty();
    }
}