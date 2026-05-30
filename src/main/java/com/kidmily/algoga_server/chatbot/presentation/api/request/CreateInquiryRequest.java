// chatbot/presentation/api/request/CreateInquiryRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateInquiryRequest(
        @NotBlank(message = "문의 내용은 필수입니다.")
        @Schema(description = "사용자가 직접 작성한 1:1 문의 내용", example = "환불 절차에 대해 상세히 알려주세요.")
        String question
) {}