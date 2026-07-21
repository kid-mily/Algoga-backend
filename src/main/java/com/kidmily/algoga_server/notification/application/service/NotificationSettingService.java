package com.kidmily.algoga_server.notification.application.service;

import com.kidmily.algoga_server.notification.application.command.UpdateNotificationSettingCommand;
import com.kidmily.algoga_server.notification.application.usecase.NotificationSettingCommandUseCase;
import com.kidmily.algoga_server.notification.application.usecase.NotificationSettingQueryUseCase;
import com.kidmily.algoga_server.notification.domain.model.NotificationSetting;
import com.kidmily.algoga_server.notification.domain.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingService implements NotificationSettingQueryUseCase, NotificationSettingCommandUseCase {

    private final NotificationSettingRepository notificationSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationSetting getSetting(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> NotificationSetting.createDefault(userId));
    }

    @Override
    @Transactional
    public NotificationSetting updateSetting(UpdateNotificationSettingCommand command) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(command.userId())
                .orElseGet(() -> NotificationSetting.createDefault(command.userId()));

        setting.update( command.learningEnabled(),
                command.qnaEnabled(),
                command.communityEnabled(),
                command.noticeEnabled(),
                command.inquiryEnabled(),
                command.friendEnabled());

        return notificationSettingRepository.save(setting);
    }
}