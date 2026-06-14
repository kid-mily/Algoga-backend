// chatbot/presentation/advice/ChatbotExceptionAdvice.java
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

    // 🌟 ChatbotException이 발생하면 공통 비즈니스 예외 처리기로 위임하여 표준화된 에러를 반환합니다.
    @ExceptionHandler(ChatbotException.class)
    public ResponseEntity<ErrorResponse> handleChatbotException(ChatbotException e) {
        return handleBusinessException(e);
    }
}