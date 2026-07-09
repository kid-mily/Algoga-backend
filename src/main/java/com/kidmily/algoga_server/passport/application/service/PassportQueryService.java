package com.kidmily.algoga_server.passport.application.service;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.passport.application.usecase.PassportQueryUseCase;
import com.kidmily.algoga_server.passport.domain.model.Passport;
import com.kidmily.algoga_server.passport.domain.repository.PassportRepository;
import com.kidmily.algoga_server.passport.exception.PassportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassportQueryService implements PassportQueryUseCase {

    private final PassportRepository passportRepository;

    @Override
    public Passport getMyPassport(Long userId) {
        return passportRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(PassportErrorCode.PASSPORT_NOT_FOUND));
    }
}
