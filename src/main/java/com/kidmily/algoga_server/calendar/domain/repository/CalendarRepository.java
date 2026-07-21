package com.kidmily.algoga_server.calendar.domain.repository;

import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository {
    Calendar save(Calendar calendar);
    List<Calendar> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    void deleteByUserIdAndReferenceIdAndType(Long userId, Long referenceId, CalendarType type);
    Calendar update(Calendar calendar);
    void deleteAllByUserId(Long userId);
    List<Calendar> findFlightRemindTargets(LocalDate startDate, LocalDate endDate);
}