package com.kidmily.algoga_server.calendar.infrastructure.mapper;

import com.kidmily.algoga_server.calendar.domain.model.Calendar;
import com.kidmily.algoga_server.calendar.infrastructure.persistence.entity.CalendarJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CalendarMapper {

    default CalendarJpaEntity toJpaEntity(Calendar calendar) {
        if (calendar == null) return null;
        return CalendarJpaEntity.builder()
                .userId(calendar.getUserId())
                .referenceId(calendar.getReferenceId())
                .eventDate(calendar.getEventDate())
                .type(calendar.getType())
                .isDDayAlertSent(calendar.getIsDDayAlertSent())
                .build();
    }

    default Calendar toDomain(CalendarJpaEntity entity) {
        if (entity == null) return null;
        return Calendar.reconstitute(
                entity.getCalendarId(),
                entity.getUserId(),
                entity.getReferenceId(),
                entity.getEventDate(),
                entity.getType(),
                entity.getIsDDayAlertSent(),
                entity.getCreatedAt()
        );
    }
}