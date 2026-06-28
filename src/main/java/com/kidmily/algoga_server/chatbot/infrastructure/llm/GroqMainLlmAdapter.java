package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GroqMainLlmAdapter implements MainLlmPort {

    private final RestClient restClient;
    private final String model;

    public GroqMainLlmAdapter(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model
    ) {
        this.model = model;
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(3000));
        requestFactory.setReadTimeout(Duration.ofMillis(12000));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @CircuitBreaker(name = "groqLlmApi", fallbackMethod = "llmFallback")
    public String generateAnswer(String question, String context, List<ChatLog> chatHistory) {
        log.info("[Main LLM] Groq HTTP API 호출... 질문: {}", question);

        String systemPrompt = """
                당신은 알고가(Algoga)의 고객 센터 전용 AI 챗봇입니다.
                모든 답변은 한국어(Korean)로 작성하며, 친절하고 명확하게 답변해야 합니다.
                해결할 수 없는 문제나 불만이 접수되면 고객센터 번호(1588-XXXX)를 안내하세요.
                
                [참고 지식]
                %s
                """.formatted(context.isBlank() ? "관련 지식 없음" : context);

        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        for (ChatLog log : chatHistory) {
            if (!log.isFiltered()) {
                messages.add(Map.of("role", "user", "content", log.getQuestion()));
                messages.add(Map.of("role", "assistant", "content", log.getAnswer()));
            }
        }

        messages.add(Map.of("role", "user", "content", question));

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "temperature", 0.7,
                "messages", messages
        );

        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String llmFallback(String question, String context, List<ChatLog> chatHistory, Throwable t) {
        log.error("[Main LLM] 서킷 브레이커 작동 또는 외부 API 호출 실패 (원인: {})", t.getMessage());
        return "현재 AI 상담 서버에 접속자가 많아 연결이 지연되고 있습니다. 잠시 후 다시 시도해주시거나, 고객센터(1588-XXXX)로 직접 문의해주세요.";
    }
}