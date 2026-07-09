package com.kidmily.algoga_server.passport.infrastructure.mapper;

import com.kidmily.algoga_server.global.security.crypto.CryptoService;
import com.kidmily.algoga_server.passport.domain.model.Passport;
import com.kidmily.algoga_server.passport.domain.model.Sex;
import com.kidmily.algoga_server.passport.infrastructure.persistence.PassportJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 도메인 <-> JPA 엔티티 변환 매퍼.
 * 저장 시 민감 필드를 암호화하고, 조회 시 복호화하는 커스텀 로직이 필요하므로
 * MapStruct 대신 CryptoService 를 주입받는 일반 컴포넌트 매퍼로 작성한다.
 * 날짜/성별은 표준 문자열(ISO-8601, 성별 코드)로 직렬화한 뒤 암호화한다.
 */
@Component
@RequiredArgsConstructor
public class PassportMapper {

    private final CryptoService cryptoService;

    public PassportJpaEntity toJpaEntity(Passport passport) {
        if (passport == null) {
            return null;
        }
        return new PassportJpaEntity(
                passport.getId(),
                passport.getUserId(),
                cryptoService.encrypt(passport.getPassportNumber()),
                cryptoService.encrypt(passport.getSurname()),
                cryptoService.encrypt(passport.getGivenName()),
                cryptoService.encrypt(passport.getNationality()),
                cryptoService.encrypt(passport.getIssuingCountry()),
                cryptoService.encrypt(passport.getBirthDate().toString()),
                cryptoService.encrypt(passport.getSex().getCode()),
                cryptoService.encrypt(passport.getIssueDate().toString()),
                cryptoService.encrypt(passport.getExpiryDate().toString()),
                cryptoService.encrypt(passport.getPersonalNumber())
        );
    }

    public Passport toDomain(PassportJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Passport.reconstitute(
                entity.getId(),
                entity.getUserId(),
                cryptoService.decrypt(entity.getPassportNumber()),
                cryptoService.decrypt(entity.getSurname()),
                cryptoService.decrypt(entity.getGivenName()),
                cryptoService.decrypt(entity.getNationality()),
                cryptoService.decrypt(entity.getIssuingCountry()),
                LocalDate.parse(cryptoService.decrypt(entity.getBirthDate())),
                Sex.fromCode(cryptoService.decrypt(entity.getSex())),
                LocalDate.parse(cryptoService.decrypt(entity.getIssueDate())),
                LocalDate.parse(cryptoService.decrypt(entity.getExpiryDate())),
                cryptoService.decrypt(entity.getPersonalNumber())
        );
    }
}
