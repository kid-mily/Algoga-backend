
package com.kidmily.algoga_server.calendar.infrastructure.persistence;

import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.domain.model.CalendarType;
import com.kidmily.algoga_server.calendar.domain.repository.CalendarRepository;
import com.kidmily.algoga_server.calendar.infrastructure.mapper.CalendarMapper;
import com.kidmily.algoga_server.calendar.infrastructure.persistence.entity.CalendarJpaEntity;
import com.kidmily.algoga_server.calendar.infrastructure.persistence.repository.SpringDataCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CalendarRepositoryAdapter implements CalendarRepository {

    private final SpringDataCalendarRepository springDataRepository;
    private final CalendarMapper calendarMapper;

    @Override
    public Calendar save(Calendar calendar) {
        return calendarMapper.toDomain(
                springDataRepository.save(calendarMapper.toJpaEntity(calendar))
        );
    }

    @Override
    public List<Calendar> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return springDataRepository.findByUserIdAndEventDateBetween(userId, startDate, endDate)
                .stream()
                .map(calendarMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndReferenceIdAndType(Long userId, Long referenceId, CalendarType type) {
        springDataRepository.deleteByUserIdAndReferenceIdAndType(userId, referenceId, type);
    }

    @Override
    public List<Calendar> findByType(CalendarType type) {
        return springDataRepository.findByType(type)
                .stream()
                .map(calendarMapper::toDomain)
                .toList();
    }
    @Override
    public Calendar update(Calendar calendar) {
        CalendarJpaEntity entity = springDataRepository.findById(calendar.getCalendarId())
                .orElseThrow(() -> new RuntimeException("캘린더를 찾을 수 없습니다."));
        entity.updateDDayAlertSent(calendar.getIsDDayAlertSent());
        return calendarMapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        springDataRepository.deleteAllByUserId(userId);
    }
}