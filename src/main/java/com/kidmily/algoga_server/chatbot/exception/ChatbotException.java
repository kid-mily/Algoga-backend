// chatbot/exception/ChatbotException.java
package com.kidmily.algoga_server.chatbot.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;

/**
 * Chatbot(AI 챗봇) 도메인 전용 커스텀 예외 클래스.
 * 글로벌 BusinessException을 상속받아 공통 에러 응답 포맷을 유지합니다.
 */
public class ChatbotException extends BusinessException {
    
    public ChatbotException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}