// chatbot/presentation/api/request/UpdateJudgmentQuestionRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateJudgmentQuestionRequest(
        @NotBlank(message = "질문은 필수입니다.") @Size(max = 1000) String question,
        @NotBlank(message = "답변은 필수입니다.") @Size(max = 2000) String answer
) {}