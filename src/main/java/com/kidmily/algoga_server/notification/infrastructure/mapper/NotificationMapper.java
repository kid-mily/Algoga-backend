package com.kidmily.algoga_server.notification.infrastructure.mapper;

import com.kidmily.algoga_server.notification.domain.model.Notification;
import com.kidmily.algoga_server.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    default NotificationJpaEntity toJpaEntity(Notification notification) {
        if (notification == null) return null;
        return NotificationJpaEntity.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .build();
    }

    default Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) return null;
        return Notification.reconstitute(
                entity.getNotificationId(),
                entity.getUserId(),
                entity.getType(),
                entity.getMessage(),
                entity.getIsRead(),
                entity.getCreatedAt()
        );
    }
}