package com.kidmily.algoga_server.chatbot.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatbotErrorCode implements BaseErrorCode {

    DAILY_USAGE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "CHAT_001", "1일 챗봇 상담 횟수를 초과했습니다."),
    LLM_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_002", "현재 상담 서버와의 연결이 원활하지 않습니다."),
    VECTOR_DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_003", "유사도 검색 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}