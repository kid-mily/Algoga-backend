package com.kidmily.algoga_server.notification.infrastructure.persistence;

import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.domain.repository.NotificationSettingRepository;
import com.kidmily.algoga_server.notification.infrastructure.mapper.NotificationSettingMapper;
import com.kidmily.algoga_server.notification.infrastructure.persistence.entity.NotificationSettingJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationSettingRepositoryAdapter implements NotificationSettingRepository {

    private final SpringDataNotificationSettingRepository springDataRepository;
    private final NotificationSettingMapper notificationSettingMapper;

    @Override
    public NotificationSetting save(NotificationSetting setting) {
        NotificationSettingJpaEntity entity = notificationSettingMapper.toJpaEntity(setting);
        NotificationSettingJpaEntity saved = springDataRepository.save(entity);
        return notificationSettingMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationSetting> findByUserId(Long userId) {
        return springDataRepository.findById(userId)
                .map(notificationSettingMapper::toDomain);
    }
}