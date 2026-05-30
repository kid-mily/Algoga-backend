// chatbot/infrastructure/llm/GroqMainLlmAdapter.java
package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
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

    // 🌟 Spring AI 의존성 없이, yml에 적어둔 값만 String으로 순수하게 읽어옵니다.
    public GroqMainLlmAdapter(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model
    ) {
        this.model = model;

        // 🌟 헤더에 API 키만 넣어서 순수 HTTP 클라이언트를 세팅합니다.
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String generateAnswer(String question) {
        log.info("[Main LLM] Groq HTTP API 직접 호출... 모델: {}, 질문: {}", model, question);

        String systemPrompt = """
                당신은 알고가(Algoga)의 고객 센터 전용 AI 챗봇입니다.
                모든 답변은 한국어(Korean)로 작성하며, 친절하고 명확하게 답변해야 합니다.
                해결할 수 없는 문제나 불만이 접수되면 고객센터 번호(1588-XXXX)를 안내하세요.
                """;

        // 1. OpenAI (Groq) API 규격에 맞는 JSON 바디(Map) 조립
        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "temperature", 0.7,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", question)
                )
        );

        try {
            // 2. 외부 API로 POST 요청 쏘고, 응답을 Map으로 받아옴
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // 3. JSON 구조에서 텍스트 알맹이만 쏙 빼내기 (choices[0].message.content)
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("[Main LLM] 외부 API 호출 중 오류 발생: {}", e.getMessage());
            return "현재 상담 서버와의 연결이 지연되고 있습니다. 잠시 후 다시 시도해주시거나, 고객센터(1588-XXXX)로 문의해주세요.";
        }
    }
}