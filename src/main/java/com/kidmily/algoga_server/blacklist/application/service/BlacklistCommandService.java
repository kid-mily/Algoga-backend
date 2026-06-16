package com.kidmily.algoga_server.blacklist.application.service;

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
        // 예외 처리 1: 이미 블랙리스트인지 검증
        if (blacklistRepository.existsByUserIdAndStatus(command.userId(), BlacklistStatus.ACTIVE)) {
            throw new BlacklistException(BlacklistErrorCode.ALREADY_BLACKLISTED);
        }

        // 예외 처리 2: 신고 횟수 5회 이상 검증
        long reportCount = reportPort.getCompletedReportCount(command.userId());
        if (reportCount < 5) {
            throw new BlacklistException(BlacklistErrorCode.NOT_ENOUGH_REPORTS);
        }

        Blacklist blacklist = Blacklist.create(command.userId(), command.reason());
        blacklistRepository.save(blacklist);

        eventPublisher.publishEvent(new UserBlacklistedEvent(command.userId()));
    }

    @Override
    public void deregisterBlacklist(Long userId) {
        // 예외 처리 3: 등록된 블랙리스트 내역이 없으면 해제 불가
        Blacklist blacklist = blacklistRepository.findByUserIdAndStatus(userId, BlacklistStatus.ACTIVE)
                .orElseThrow(() -> new BlacklistException(BlacklistErrorCode.BLACKLIST_NOT_FOUND));

        blacklist.deregister();
        blacklistRepository.save(blacklist);

        eventPublisher.publishEvent(new UserUnblacklistedEvent(userId));
    }
}