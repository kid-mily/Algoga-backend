package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Primary // 🌟 기존 Groq 어댑터 대신 이 빈(Bean)이 최우선으로 주입되도록 합니다.
@Profile({"local", "loadtest", "test"}) // 🌟 운영(prod) 환경에서는 작동하지 않도록 보호합니다.
@Component
public class DummyMainLlmAdapter implements MainLlmPort {

    @Override
    public String generateAnswer(String question, String context, List<ChatLog> chatHistory) {
        log.info("[Dummy LLM] 실제 API 호출 없이 가짜 응답을 생성합니다. 질문: {}", question);

        // 실제 LLM이 답변을 생성하는 시간(약 1.5초)을 시뮬레이션하여 
        // 아키텍처 및 캐시 테스트의 정확도를 높입니다.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 컨텍스트(약관)가 잘 넘어왔는지 확인하기 위해 덧붙여서 반환합니다.
        return "[더미 응답] 환불은 결제 후 7일 이내에 가능합니다. (참고한 RAG 지식: " + context + ")";
    }
}