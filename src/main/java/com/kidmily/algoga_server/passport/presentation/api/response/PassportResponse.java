package com.kidmily.algoga_server.passport.presentation.api.response;

import com.kidmily.algoga_server.passport.domain.model.Passport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 여권 정보 응답. 보안을 위해 민감 필드는 원문 대신 마스킹(*)된 값만 노출한다.
 * 복호화된 원문은 서버 내부에서만 다루며 API 응답으로 그대로 나가지 않는다.
 */
@Schema(description = "여권 정보 응답 (민감 정보는 마스킹 처리됨)")
public record PassportResponse(

        @Schema(description = "여권 정보 식별자(ID)", example = "1")
        Long passportId,

        @Schema(description = "여권번호(마스킹)", example = "M*******8")
        String passportNumber,

        @Schema(description = "성(마스킹)", example = "H***")
        String surname,

        @Schema(description = "이름(마스킹)", example = "G******")
        String givenName,

        @Schema(description = "국적", example = "KOR")
        String nationality,

        @Schema(description = "발급 국가", example = "KOR")
        String issuingCountry,

        @Schema(description = "생년월일(연도만 노출)", example = "1990-**-**")
        String birthDate,

        @Schema(description = "성별 (M/F/X)", example = "M")
        String sex,

        @Schema(description = "발급일(연도만 노출)", example = "2020-**-**")
        String issueDate,

        @Schema(description = "기간 만료일(연도만 노출)", example = "2030-**-**")
        String expiryDate,

        @Schema(description = "개인번호(마스킹)", example = "*******")
        String personalNumber
) {

    public static PassportResponse from(Passport passport) {
        return new PassportResponse(
                passport.getId(),
                maskKeepEnds(passport.getPassportNumber(), 1, 1),
                maskKeepPrefix(passport.getSurname(), 1),
                maskKeepPrefix(passport.getGivenName(), 1),
                passport.getNationality(),
                passport.getIssuingCountry(),
                maskDate(passport.getBirthDate()),
                passport.getSex().getCode(),
                maskDate(passport.getIssueDate()),
                maskDate(passport.getExpiryDate()),
                maskAll(passport.getPersonalNumber())
        );
    }

    // 앞 prefix글자, 뒤 suffix글자만 남기고 가운데를 '*'로 가린다. 길이가 짧으면 전부 가린다.
    private static String maskKeepEnds(String value, int prefix, int suffix) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        if (length <= prefix + suffix) {
            return "*".repeat(length);
        }
        return value.substring(0, prefix)
                + "*".repeat(length - prefix - suffix)
                + value.substring(length - suffix);
    }

    private static String maskKeepPrefix(String value, int prefix) {
        return maskKeepEnds(value, prefix, 0);
    }

    // 날짜는 연도만 노출하고 월/일은 가린다.
    private static String maskDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.getYear() + "-**-**";
    }

    // 전체를 '*'로 가린다. (개인번호 등 특히 민감한 값)
    private static String maskAll(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return "*".repeat(value.length());
    }
}
