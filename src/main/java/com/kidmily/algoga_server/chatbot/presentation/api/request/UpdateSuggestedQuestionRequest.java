package com.kidmily.algoga_server.chatbot.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateSuggestedQuestionRequest(@NotBlank String question, @NotBlank String answer) {}