// chatbot/presentation/api/response/SuggestedQuestionResponse.java
package com.kidmily.algoga_server.chatbot.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회용 예상 질문 버튼 응답 DTO")
public record SuggestedQuestionResponse(
        @Schema(description = "예상 질문 고유 ID", example = "1")
        Long suggestedQuestionId,

        @Schema(description = "프론트엔드 버튼에 표시될 질문 텍스트", example = "환불 규정이 궁금해요.")
        String question
) {}