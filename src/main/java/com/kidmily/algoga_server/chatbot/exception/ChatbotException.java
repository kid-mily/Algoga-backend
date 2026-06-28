package com.kidmily.algoga_server.chatbot.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;

public class ChatbotException extends BusinessException {
    public ChatbotException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}