package com.kidmily.algoga_server.passport.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여권 JPA 엔티티.
 * userId 를 제외한 모든 여권 필드는 암호화된 문자열(암호문) 상태로 저장된다.
 * 암호화/복호화는 PassportMapper 가 CryptoService 를 통해 수행하며,
 * 이 엔티티는 이미 암호화된 값을 그대로 보관한다. (날짜/성별도 문자열 암호문으로 저장)
 */
@Entity
@Table(
        name = "passports",
        uniqueConstraints = @UniqueConstraint(name = "uk_passports_user_id", columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 조회 기준이므로 평문으로 저장한다. (그 자체로는 개인 식별 정보가 아님)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "passport_number", nullable = false, length = 512)
    private String passportNumber;

    @Column(nullable = false, length = 512)
    private String surname;

    @Column(name = "given_name", nullable = false, length = 512)
    private String givenName;

    @Column(nullable = false, length = 512)
    private String nationality;

    @Column(name = "issuing_country", nullable = false, length = 512)
    private String issuingCountry;

    @Column(name = "birth_date", nullable = false, length = 512)
    private String birthDate;

    @Column(nullable = false, length = 512)
    private String sex;

    @Column(name = "issue_date", nullable = false, length = 512)
    private String issueDate;

    @Column(name = "expiry_date", nullable = false, length = 512)
    private String expiryDate;

    @Column(name = "personal_number", length = 512)
    private String personalNumber;

    public PassportJpaEntity(Long id, Long userId, String passportNumber, String surname, String givenName,
                             String nationality, String issuingCountry, String birthDate, String sex,
                             String issueDate, String expiryDate, String personalNumber) {
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
}
