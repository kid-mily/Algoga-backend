package com.kidmily.algoga_server.inquiry.presentation.api.request;
import jakarta.validation.constraints.NotBlank;
public record AnswerInquiryRequest(@NotBlank String answer) {}