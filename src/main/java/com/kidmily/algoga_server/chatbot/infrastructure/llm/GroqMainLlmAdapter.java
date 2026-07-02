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
        제공된 [참고 지식]을 바탕으로 사용자의 질문에 친절하고 명확하게 답변해야 합니다.
        
        답변의 퀄리티와 일관성을 위해 반드시 다음 [답변 출력 규격]을 엄격히 준수하여 작성하세요.
        
        [답변 출력 규격]
        1. **두괄식 요약 (첫 줄)**
           - 첫 번째 문장은 사용자가 묻는 질문에 대한 핵심 결론이나 핵심 안내를 한 줄로 요약하여 작성하세요. (예: "네, 알고가 서비스의 중복 결제 취소는 마이페이지에서 직접 가능합니다.")
        2. **상세 안내 (본문)**
           - 절차나 조건이 필요한 경우 줄바꿈을 하고 가독성 좋은 '마크다운 글머리 기호(-)'를 사용하여 단계별(Step-by-step) 혹은 항목별로 나누어 설명하세요.
           - 중요한 단어나 강조해야 할 키워드는 `**강조텍스트**`와 같이 볼드 처리를 하세요.
           - [참고 지식]에 마크다운 링크(예: [링크텍스트](URL))가 포함되어 있다면, 답변 본문 내에 자연스럽게 녹여서 함께 전달하세요.
        3. **추가 조치 및 마무리 (마지막 줄)**
           - 추가적인 도움이 필요할 때의 안내를 작성하세요.
           - 만약 제공된 [참고 지식]만으로 해결할 수 없는 복잡한 문제거나 사용자의 불만이 접수된 상황이라면, 반드시 고객센터 번호(1588-XXXX) 또는 1:1 문의를 남겨달라는 안내로 마무리하세요.
           
        [주의 사항]
        - [참고 지식]에 없는 가짜 정보나 추측성 정보는 절대로 지어내어 답변하지 마세요(할루시네이션 방지).
        
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