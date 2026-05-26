package com.kidmily.algoga_server.calendar.infrastructure.persistence.repository;//package com.kidmily.algoga_server.calendar.infrastructure.persistence.repository;

import com.kidmily.algoga_server.calendar.infrastructure.persistence.entity.CalendarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataCalendarRepository extends JpaRepository<CalendarJpaEntity, Long> {
    List<CalendarJpaEntity> findByUserIdAndEventDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );
    void deleteByReferenceId(Long referenceId);
}