// chatbot/presentation/api/response/SuggestedQuestionResponse.java
package com.kidmily.algoga_server.chatbot.presentation.api.response;

public record SuggestedQuestionResponse(
        Long suggestedQuestionId,
        String question
) {}