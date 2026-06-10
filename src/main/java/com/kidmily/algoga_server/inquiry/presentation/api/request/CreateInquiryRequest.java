package com.kidmily.algoga_server.inquiry.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInquiryRequest(
        @NotBlank(message = "문의 내용은 필수입니다.")
        @Size(max = 2000, message = "내용은 2000자 이내여야 합니다.")
        @Schema(description = "1:1 문의 내용", example = "환불 절차에 대해 상세히 알려주세요.")
        String question
) {}