package com.kidmily.algoga_server.chatbot.application.port.out;

/**
 * 실제 양질의 비즈니스 답변을 생성하는 메인 LLM 포트 (Gemini, GPT 등)
 */
public interface MainLlmPort {
    String generateAnswer(String question);
}