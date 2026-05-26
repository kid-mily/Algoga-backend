package com.kidmily.algoga_server.calendar.domain.repository;

import com.kidmily.algoga_server.calendar.domain.model.Calendar;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository {
    Calendar save(Calendar calendar);
    List<Calendar> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    void deleteByReferenceId(Long referenceId);
}