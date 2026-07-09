package com.kidmily.algoga_server.passport.application.service;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.passport.application.command.RegisterPassportCommand;
import com.kidmily.algoga_server.passport.application.command.UpdatePassportCommand;
import com.kidmily.algoga_server.passport.application.usecase.PassportCommandUseCase;
import com.kidmily.algoga_server.passport.domain.model.Passport;
import com.kidmily.algoga_server.passport.domain.repository.PassportRepository;
import com.kidmily.algoga_server.passport.exception.PassportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PassportCommandService implements PassportCommandUseCase {

    private final PassportRepository passportRepository;

    @Override
    public Passport register(RegisterPassportCommand command) {
        // 한 사용자는 하나의 여권만 등록할 수 있다.
        if (passportRepository.existsByUserId(command.userId())) {
            throw new BusinessException(PassportErrorCode.PASSPORT_ALREADY_EXISTS);
        }

        Passport passport = Passport.create(
                command.userId(),
                command.passportNumber(),
                command.surname(),
                command.givenName(),
                command.nationality(),
                command.issuingCountry(),
                command.birthDate(),
                command.sex(),
                command.issueDate(),
                command.expiryDate(),
                command.personalNumber()
        );

        return passportRepository.save(passport);
    }

    @Override
    public Passport update(UpdatePassportCommand command) {
        Passport passport = passportRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BusinessException(PassportErrorCode.PASSPORT_NOT_FOUND));

        passport.updateInfo(
                command.passportNumber(),
                command.surname(),
                command.givenName(),
                command.nationality(),
                command.issuingCountry(),
                command.birthDate(),
                command.sex(),
                command.issueDate(),
                command.expiryDate(),
                command.personalNumber()
        );

        return passportRepository.save(passport);
    }
}
