package com.kidmily.algoga_server.passport.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "여권 정보 등록 요청")
public record RegisterPassportRequest(

        @Schema(description = "여권번호", example = "M12345678")
        @NotBlank(message = "여권번호는 필수입니다.")
        @Pattern(regexp = "^[A-Z0-9]{6,9}$", message = "여권번호는 영문 대문자와 숫자 6~9자리여야 합니다.")
        String passportNumber,

        @Schema(description = "성(영문, 여권 표기)", example = "HONG")
        @NotBlank(message = "성(영문)은 필수입니다.")
        @Pattern(regexp = "^[A-Za-z][A-Za-z '-]{0,38}$", message = "성은 영문/공백/'/- 로만 입력할 수 있습니다.")
        String surname,

        @Schema(description = "이름(영문, 여권 표기)", example = "GILDONG")
        @NotBlank(message = "이름(영문)은 필수입니다.")
        @Pattern(regexp = "^[A-Za-z][A-Za-z '-]{0,38}$", message = "이름은 영문/공백/'/- 로만 입력할 수 있습니다.")
        String givenName,

        @Schema(description = "국적 (ISO 3166-1 alpha-3)", example = "KOR")
        @NotBlank(message = "국적은 필수입니다.")
        @Pattern(regexp = "^[A-Z]{3}$", message = "국적은 영문 대문자 3자리 국가코드여야 합니다.")
        String nationality,

        @Schema(description = "발급 국가 (ISO 3166-1 alpha-3)", example = "KOR")
        @NotBlank(message = "발급 국가는 필수입니다.")
        @Pattern(regexp = "^[A-Z]{3}$", message = "발급 국가는 영문 대문자 3자리 국가코드여야 합니다.")
        String issuingCountry,

        @Schema(description = "생년월일", example = "1990-05-21")
        @NotNull(message = "생년월일은 필수입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @Schema(description = "성별 (M: 남성, F: 여성, X: 미지정)", example = "M")
        @NotBlank(message = "성별은 필수입니다.")
        @Pattern(regexp = "^[MFX]$", message = "성별은 M, F, X 중 하나여야 합니다.")
        String sex,

        @Schema(description = "발급일", example = "2020-05-21")
        @NotNull(message = "발급일은 필수입니다.")
        @PastOrPresent(message = "발급일은 오늘이거나 과거 날짜여야 합니다.")
        LocalDate issueDate,

        @Schema(description = "기간 만료일", example = "2030-05-20")
        @NotNull(message = "만료일은 필수입니다.")
        LocalDate expiryDate,

        @Schema(description = "개인번호(선택)", example = "1234567")
        @Pattern(regexp = "^[A-Z0-9]{0,14}$", message = "개인번호는 영문 대문자와 숫자 최대 14자리여야 합니다.")
        String personalNumber
) {
}
