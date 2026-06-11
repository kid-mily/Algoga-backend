// User Request DTO
package com.kidmily.algoga_server.inquiry.presentation.api.request;

import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInquiryRequest(
        @NotNull(message = "카테고리를 선택해주세요.")
        @Schema(description = "문의 카테고리", example = "REFUND")
        InquiryCategory category,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
        @Schema(description = "문의 제목", example = "환불 절차 문의드립니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 2000, message = "내용은 2000자 이내여야 합니다.")
        @Schema(description = "1:1 문의 내용", example = "어제 결제했는데 환불 규정이 어떻게 되나요?")
        String content
) {}