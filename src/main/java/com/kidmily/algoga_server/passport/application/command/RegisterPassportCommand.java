package com.kidmily.algoga_server.passport.application.command;

import com.kidmily.algoga_server.passport.domain.model.Sex;

import java.time.LocalDate;

// 여권 등록 요청 DTO (Presentation 기술에 의존하지 않음)
public record RegisterPassportCommand(
        Long userId,
        String passportNumber,
        String surname,
        String givenName,
        String nationality,
        String issuingCountry,
        LocalDate birthDate,
        Sex sex,
        LocalDate issueDate,
        LocalDate expiryDate,
        String personalNumber
) {
}
