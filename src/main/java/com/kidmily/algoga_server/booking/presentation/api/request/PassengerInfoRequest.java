package com.kidmily.algoga_server.booking.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "탑승객(여권) 정보 — 이름/신분증은 실제 여권과 일치해야 함")
public record PassengerInfoRequest(

        @Schema(description = "성 (여권 표기)", example = "KIM")
        String lastName,

        @Schema(description = "이름 (여권 표기)", example = "YEOHAENG")
        String firstName,

        @Schema(description = "생년월일", example = "1995-03-21")
        LocalDate birthDate,

        @Schema(description = "여권 번호", example = "M12345678")
        String passportNumber,

        @Schema(description = "여권 만료일", example = "2030-03-20")
        LocalDate passportExpiry
) {}
