package com.kidmily.algoga_server.booking.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "탑승객(여권) 정보 — 이름/신분증은 실제 여권과 일치해야 함")
public record PassengerInfoRequest(

        @Schema(description = "성 (여권 표기)", example = "KIM")
        @NotBlank(message = "여권 성(영문)은 필수입니다.")
        String lastName,

        @Schema(description = "이름 (여권 표기)", example = "YEOHAENG")
        @NotBlank(message = "여권 이름(영문)은 필수입니다.")
        String firstName,

        @Schema(description = "성별. \"M\"(남) | \"F\"(여)", example = "M")
        @NotBlank(message = "성별은 필수입니다.")
        String gender,

        @Schema(description = "생년월일", example = "1995-03-21")
        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @Schema(description = "국적 (현재 대한민국 고정, FE 하드코딩)", example = "대한민국")
        String nationality,

        @Schema(description = "여권 번호", example = "M12345678")
        @NotBlank(message = "여권 번호는 필수입니다.")
        String passportNumber,

        @Schema(description = "여권 만료일", example = "2030-03-20")
        @NotNull(message = "여권 만료일은 필수입니다.")
        LocalDate passportExpiry
) {}
