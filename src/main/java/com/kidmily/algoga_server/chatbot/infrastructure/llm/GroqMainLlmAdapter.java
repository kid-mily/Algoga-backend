// chatbot/infrastructure/llm/GroqMainLlmAdapter.java
package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    // 🌟 서킷 브레이커 적용: 실패율이 임계치를 넘으면 즉시 차단(Open)하고 llmFallback 메서드를 실행합니다.
    @CircuitBreaker(name = "groqLlmApi", fallbackMethod = "llmFallback")
    public String generateAnswer(String question) {
        log.info("[Main LLM] Groq HTTP API 직접 호출... 모델: {}, 질문: {}", model, question);

        String systemPrompt = """
                당신은 알고가(Algoga)의 고객 센터 전용 AI 챗봇입니다.
                모든 답변은 한국어(Korean)로 작성하며, 친절하고 명확하게 답변해야 합니다.
                해결할 수 없는 문제나 불만이 접수되면 고객센터 번호(1588-XXXX)를 안내하세요.
                """;

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "temperature", 0.7,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", question)
                )
        );

        // 외부 API 호출 (try-catch 제거! 실패 시 예외가 발생해야 서킷브레이커가 실패 카운트를 올립니다)
        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        // JSON 구조에서 텍스트 알맹이만 추출
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    // 🌟 Fallback 메서드: 서킷 브레이커가 열렸거나(Open), 타임아웃/서버 에러 등이 발생했을 때 실행됨
    // 주의: 파라미터 구성이 원본 메서드(String) + Throwable 형태여야 합니다.
    private String llmFallback(String question, Throwable t) {
        log.error("[Main LLM] 서킷 브레이커 작동 또는 외부 API 호출 실패 (원인: {})", t.getMessage());
        return "현재 AI 상담 서버에 접속자가 많아 연결이 지연되고 있습니다. 잠시 후 다시 시도해주시거나, 고객센터(1588-XXXX)로 직접 문의해주세요.";
    }
}