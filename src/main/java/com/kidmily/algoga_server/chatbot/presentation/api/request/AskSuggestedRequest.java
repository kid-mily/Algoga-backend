// chatbot/presentation/api/request/AskSuggestedRequest.java
package com.kidmily.algoga_server.chatbot.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AskSuggestedRequest(
        @NotNull(message = "예상 질문 ID는 필수입니다.")
        @Schema(description = "클릭한 예상 질문 버튼의 고유 ID", example = "1")
        Long suggestedQuestionId
) {}