package com.kidmily.algoga_server.blacklist.application.service;

import com.kidmily.algoga_server.blacklist.application.command.DeregisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.command.RegisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.port.BlacklistReportPort;
import com.kidmily.algoga_server.blacklist.application.usecase.BlacklistCommandUseCase;
import com.kidmily.algoga_server.blacklist.domain.model.Blacklist;
import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;
import com.kidmily.algoga_server.blacklist.domain.repository.BlacklistRepository;
import com.kidmily.algoga_server.blacklist.exception.BlacklistErrorCode;
import com.kidmily.algoga_server.blacklist.exception.BlacklistException;
import com.kidmily.algoga_server.global.event.UserBlacklistedEvent;
import com.kidmily.algoga_server.global.event.UserUnblacklistedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlacklistCommandService implements BlacklistCommandUseCase {

    private final BlacklistRepository blacklistRepository;
    private final BlacklistReportPort reportPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void registerBlacklist(RegisterBlacklistCommand command) {
        if (blacklistRepository.existsByUserIdAndStatus(command.userId(), BlacklistStatus.ACTIVE)) {
            throw new BlacklistException(BlacklistErrorCode.ALREADY_BLACKLISTED);
        }

        long reportCount = reportPort.getCompletedReportCount(command.userId());
        if (reportCount < 5) {
            throw new BlacklistException(BlacklistErrorCode.NOT_ENOUGH_REPORTS);
        }

        Blacklist blacklist = Blacklist.create(command.userId(), command.reason());
        blacklistRepository.save(blacklist);

        eventPublisher.publishEvent(new UserBlacklistedEvent(command.userId()));
    }

    @Override
    public void deregisterBlacklist(DeregisterBlacklistCommand command) {
        // 🌟 수정됨: command.userId() 로 값 추출
        Blacklist blacklist = blacklistRepository.findByUserIdAndStatus(command.userId(), BlacklistStatus.ACTIVE)
                .orElseThrow(() -> new BlacklistException(BlacklistErrorCode.BLACKLIST_NOT_FOUND));

        blacklist.deregister();
        blacklistRepository.save(blacklist);

        eventPublisher.publishEvent(new UserUnblacklistedEvent(command.userId()));
    }
}