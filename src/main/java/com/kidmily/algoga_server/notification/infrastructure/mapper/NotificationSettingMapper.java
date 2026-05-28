package com.kidmily.algoga_server.notification.infrastructure.mapper;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.infrastructure.persistence.entity.NotificationSettingJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationSettingMapper {

    default NotificationSettingJpaEntity toJpaEntity(NotificationSetting setting) {
        if (setting == null) return null;
        return NotificationSettingJpaEntity.builder()
                .userId(setting.getUserId())
                .learningEnabled(setting.getLearningEnabled())
                .qnaEnabled(setting.getQnaEnabled())
                .communityEnabled(setting.getCommunityEnabled())
                .noticeEnabled(setting.getNoticeEnabled())
                .inquiryEnabled(setting.getInquiryEnabled())
                .friendEnabled(setting.getFriendEnabled())
                .build();
    }

    default NotificationSetting toDomain(NotificationSettingJpaEntity entity) {
        if (entity == null) return null;
        return NotificationSetting.reconstitute(
                entity.getUserId(),
                entity.getLearningEnabled(),
                entity.getQnaEnabled(),
                entity.getCommunityEnabled(),
                entity.getNoticeEnabled(),
                entity.getInquiryEnabled(),
                entity.getFriendEnabled()
        );
    }
}