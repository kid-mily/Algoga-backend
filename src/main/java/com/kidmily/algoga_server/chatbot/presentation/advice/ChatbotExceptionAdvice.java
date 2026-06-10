// chatbot/presentation/advice/ChatbotExceptionAdvice.java
package com.kidmily.algoga_server.chatbot.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 🌟 Chatbot 도메인의 컨트롤러에서 발생하는 예외만 전담하여 처리
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.chatbot.presentation.api")
public class ChatbotExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log; 
    }
    
    // (LLM_SERVER_ERROR, VECTOR_DB_ERROR 등은 BusinessException으로 던져지면 
    // 부모 인터페이스의 default 메서드가 알아서 ApiResponse 규격으로 만들어 클라이언트에 반환합니다.)
}