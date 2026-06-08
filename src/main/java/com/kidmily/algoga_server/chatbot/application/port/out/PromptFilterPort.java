package com.kidmily.algoga_server.chatbot.application.port.out;

/**
 * 터무니없는 질문(비속어, 아무말, 시스템 무관 질문)을 걸러내는 가벼운 온디바이스 LLM 포트
 */
public interface PromptFilterPort {
    boolean isValidQuestion(String question);
}