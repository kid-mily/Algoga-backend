package com.kidmily.algoga_server.chatbot.presentation.advice;

import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.chatbot.presentation.api")
public class ChatbotExceptionAdvice implements CommonExceptionAdvice {
    
    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler(ChatbotException.class)
    public ResponseEntity<ErrorResponse> handleChatbotException(ChatbotException e) {
        return handleBusinessException(e);
    }
}