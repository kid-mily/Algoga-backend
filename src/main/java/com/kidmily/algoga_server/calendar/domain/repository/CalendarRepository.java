package com.kidmily.algoga_server.calendar.domain.repository;

import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository {
    Calendar save(Calendar calendar);
    List<Calendar> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    // 기존 단일 referenceId 삭제에서 -> 안전한 복합 조건 삭제 포트로 변경
    void deleteByUserIdAndReferenceIdAndType(Long userId, Long referenceId, CalendarType type);
    List<Calendar> findByType(CalendarType type);
    Calendar update(Calendar calendar);
    void deleteAllByUserId(Long userId);
}