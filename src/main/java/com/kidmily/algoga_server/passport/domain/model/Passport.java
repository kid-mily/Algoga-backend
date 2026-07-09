package com.kidmily.algoga_server.passport.domain.model;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.passport.exception.PassportErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 여권 도메인 모델. 한 사용자는 하나의 여권 정보를 가진다.
 * 순수 POJO이며 영속/암호화 기술에 의존하지 않는다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Passport {

    private Long id;
    private Long userId;

    private String passportNumber;   // 여권번호
    private String surname;          // 성(영문)
    private String givenName;        // 이름(영문)
    private String nationality;      // 국적 (ISO 3166-1 alpha-3)
    private String issuingCountry;   // 발급 국가 (ISO 3166-1 alpha-3)
    private LocalDate birthDate;     // 생년월일
    private Sex sex;                 // 성별
    private LocalDate issueDate;     // 발급일
    private LocalDate expiryDate;    // 기간 만료일
    private String personalNumber;   // 개인번호 (선택)

    // 신규 생성용
    private Passport(Long userId, String passportNumber, String surname, String givenName,
                     String nationality, String issuingCountry, LocalDate birthDate, Sex sex,
                     LocalDate issueDate, LocalDate expiryDate, String personalNumber) {
        this.userId = userId;
        this.passportNumber = passportNumber;
        this.surname = surname;
        this.givenName = givenName;
        this.nationality = nationality;
        this.issuingCountry = issuingCountry;
        this.birthDate = birthDate;
        this.sex = sex;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.personalNumber = personalNumber;
        validate();
    }

    // 영속 데이터 복원용 (매퍼 전용)
    private Passport(Long id, Long userId, String passportNumber, String surname, String givenName,
                     String nationality, String issuingCountry, LocalDate birthDate, Sex sex,
                     LocalDate issueDate, LocalDate expiryDate, String personalNumber) {
        this.id = id;
        this.userId = userId;
        this.passportNumber = passportNumber;
        this.surname = surname;
        this.givenName = givenName;
        this.nationality = nationality;
        this.issuingCountry = issuingCountry;
        this.birthDate = birthDate;
        this.sex = sex;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.personalNumber = personalNumber;
    }

    public static Passport create(Long userId, String passportNumber, String surname, String givenName,
                                  String nationality, String issuingCountry, LocalDate birthDate, Sex sex,
                                  LocalDate issueDate, LocalDate expiryDate, String personalNumber) {
        return new Passport(userId, passportNumber, surname, givenName, nationality, issuingCountry,
                birthDate, sex, issueDate, expiryDate, personalNumber);
    }

    public static Passport reconstitute(Long id, Long userId, String passportNumber, String surname, String givenName,
                                        String nationality, String issuingCountry, LocalDate birthDate, Sex sex,
                                        LocalDate issueDate, LocalDate expiryDate, String personalNumber) {
        return new Passport(id, userId, passportNumber, surname, givenName, nationality, issuingCountry,
                birthDate, sex, issueDate, expiryDate, personalNumber);
    }

    /**
     * 여권 정보 전체 갱신. 소유자(userId)와 식별자(id)는 변경하지 않는다.
     */
    public void updateInfo(String passportNumber, String surname, String givenName,
                           String nationality, String issuingCountry, LocalDate birthDate, Sex sex,
                           LocalDate issueDate, LocalDate expiryDate, String personalNumber) {
        this.passportNumber = passportNumber;
        this.surname = surname;
        this.givenName = givenName;
        this.nationality = nationality;
        this.issuingCountry = issuingCountry;
        this.birthDate = birthDate;
        this.sex = sex;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.personalNumber = personalNumber;
        validate();
    }

    // 도메인 불변식. 형식 검증은 DTO(@Valid)에서 1차로 막고, 여기서는 필수/일관성만 최종 보증한다.
    private void validate() {
        if (userId == null) {
            throw new BusinessException(PassportErrorCode.INVALID_PASSPORT_FIELD);
        }
        requireText(passportNumber);
        requireText(surname);
        requireText(givenName);
        requireText(nationality);
        requireText(issuingCountry);
        if (birthDate == null || issueDate == null || expiryDate == null || sex == null) {
            throw new BusinessException(PassportErrorCode.INVALID_PASSPORT_FIELD);
        }
        if (!expiryDate.isAfter(issueDate)) {
            throw new BusinessException(PassportErrorCode.INVALID_EXPIRY_DATE);
        }
    }

    private void requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(PassportErrorCode.INVALID_PASSPORT_FIELD);
        }
    }
}
