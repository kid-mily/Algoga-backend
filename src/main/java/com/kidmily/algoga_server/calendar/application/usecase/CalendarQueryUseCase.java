package com.kidmily.algoga_server.calendar.application.usecase;

import com.kidmily.algoga_server.calendar.presentation.api.response.CalendarResponse;

public interface CalendarQueryUseCase {
    CalendarResponse getCalendar(Long userId, int year, int month);
}