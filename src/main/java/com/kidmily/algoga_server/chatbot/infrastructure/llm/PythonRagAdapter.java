package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.RagAnswer;
import com.kidmily.algoga_server.chatbot.application.port.out.RagPort;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 외부 Python RAG+FunctionCalling 서버(/chat)를 호출하는 어댑터.
 * 규정 검색(Chroma), 회원 데이터 조회(Spring 역호출), 상담원 연결 판단은 모두 Python 이 수행한다.
 * 여기서는 HTTP 호출과 대화 이력 변환, 장애 시 폴백만 담당한다.
 */
@Slf4j
@Component
public class PythonRagAdapter implements RagPort {

    private final RestClient restClient;

    public PythonRagAdapter(@Value("${rag.python.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(3000));
        // 검색 + LLM 생성 + (필요시) 함수호출 역호출까지 이뤄지므로 읽기 타임아웃을 넉넉히 둔다.
        requestFactory.setReadTimeout(Duration.ofMillis(60000));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @CircuitBreaker(name = "pythonRagApi", fallbackMethod = "ragFallback")
    public RagAnswer ask(String question, Long userId, List<ChatLog> chatHistory) {
        log.info("[Python RAG] /chat 호출... user={} 질문: {}", userId, question);

        // 필터링된(도메인 외) 로그는 맥락으로 넘기지 않는다.
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatLog log : chatHistory) {
            if (!log.isFiltered()) {
                history.add(Map.of("role", "user", "content", log.getQuestion()));
                history.add(Map.of("role", "assistant", "content", log.getAnswer()));
            }
        }

        Map<String, Object> requestBody = Map.of(
                "question", question,
                "user_id", userId,
                "history", history
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/chat")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Python RAG 서버 응답이 비어 있습니다.");
        }

        String answer = (String) response.getOrDefault("answer", "");
        String mode = (String) response.getOrDefault("mode", RagAnswer.MODE_NORMAL);
        String handoffSummary = (String) response.get("handoff_summary");
        String handoffInquiry = (String) response.get("handoff_inquiry");

        @SuppressWarnings("unchecked")
        List<String> usedTools = (List<String>) response.getOrDefault("used_tools", List.of());

        List<RagAnswer.RagSource> sources = parseSources(response.get("sources"));

        return new RagAnswer(answer, usedTools, mode, handoffSummary, handoffInquiry, sources);
    }

    /** Python 응답의 sources([{source, page}, ...])를 RagSource 리스트로 변환. page 는 숫자/부재 모두 허용. */
    @SuppressWarnings("unchecked")
    private List<RagAnswer.RagSource> parseSources(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<RagAnswer.RagSource> sources = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object source = map.get("source");
                Object page = map.get("page");
                sources.add(new RagAnswer.RagSource(
                        source != null ? source.toString() : null,
                        (page instanceof Number number) ? number.intValue() : null
                ));
            }
        }
        return sources;
    }

    /**
     * Python 서버 장애/타임아웃 또는 서킷 오픈 시 폴백.
     * NORMAL 모드로 안내 메시지를 그대로 사용자에게 전달한다(캐시 저장은 서비스가 개인정보 여부로 판단).
     */
    private RagAnswer ragFallback(String question, Long userId, List<ChatLog> chatHistory, Throwable t) {
        log.error("[Python RAG] 호출 실패 또는 서킷 오픈 (원인: {})", t.getMessage());
        String message = "현재 AI 상담 서버 연결이 지연되고 있습니다. "
                + "잠시 후 다시 시도해주시거나, 고객센터(1588-XXXX)로 문의해주세요.";
        return new RagAnswer(message, List.of(), RagAnswer.MODE_NORMAL, null, null, List.of());
    }
}
