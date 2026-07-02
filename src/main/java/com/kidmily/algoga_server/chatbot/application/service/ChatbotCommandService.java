package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.command.AskChatbotCommand;
import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import com.kidmily.algoga_server.chatbot.application.port.out.MainLlmPort;
import com.kidmily.algoga_server.chatbot.application.usecase.ChatbotCommandUseCase;
import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.ChatLogRepository;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.exception.ChatbotErrorCode;
import com.kidmily.algoga_server.chatbot.exception.ChatbotException;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ChatbotCommandService implements ChatbotCommandUseCase {

    private final KnowledgeRetrievalPort knowledgeRetrievalPort;
    private final MainLlmPort mainLlmPort;
    private final ChatLogRepository chatLogRepository;
    private final SuggestedQuestionRepository suggestedQuestionRepository;
    
    // 🌟 추가된 의존성: 캐시 전용 벡터 저장소 & 레디스 직접 제어용 클라이언트
    private final VectorStore semanticCacheStore;
    private final JedisPooled jedisPooled;

    private static final double SEMANTIC_CACHE_THRESHOLD = 0.95; // 95% 이상 일치 시 캐시 적중
    private static final long CACHE_TTL_SECONDS = 24 * 60 * 60;  // 개별 캐시 수명: 24시간 (초 단위)

    public ChatbotCommandService(
            KnowledgeRetrievalPort knowledgeRetrievalPort,
            MainLlmPort mainLlmPort,
            ChatLogRepository chatLogRepository,
            SuggestedQuestionRepository suggestedQuestionRepository,
            @Qualifier("semanticCacheStore") VectorStore semanticCacheStore,
            JedisPooled jedisPooled) {
        this.knowledgeRetrievalPort = knowledgeRetrievalPort;
        this.mainLlmPort = mainLlmPort;
        this.chatLogRepository = chatLogRepository;
        this.suggestedQuestionRepository = suggestedQuestionRepository;
        this.semanticCacheStore = semanticCacheStore;
        this.jedisPooled = jedisPooled;
    }

    @Override
    @Transactional
    public ChatbotAnswerResponse askToChatbot(AskChatbotCommand command) {
        String question = command.question();
        Long userId = command.userId();

        // 1. [L2 시맨틱 캐시 조회] 이전에 유사도 95% 이상의 질문을 한 적이 있는지 검사
        List<Document> cachedResults = new java.util.ArrayList<>();
        try {
            cachedResults = semanticCacheStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(1)
                            .similarityThreshold(SEMANTIC_CACHE_THRESHOLD)
                            .build()
            );
        } catch (Exception e) {
            // Redis가 다운되더라도 에러 로그만 남기고 다음 단계(Vector DB 조회)로 진행
            log.error("❌ [Semantic Cache 조회 실패] Redis 연결 오류 등으로 캐시 조회를 건너뜁니다: {}", e.getMessage());
        }

        if (!cachedResults.isEmpty()) {
            Document cachedDoc = cachedResults.get(0);
            String cachedAnswer = cachedDoc.getMetadata().get("cachedAnswer").toString();

            // 🌟 메타데이터에서 distance 추출 및 유사도(Similarity) 계산
            double similarity = 0.0;
            Object distanceObj = cachedDoc.getMetadata().get("distance");

            if (distanceObj != null) {
                double distance = Double.parseDouble(distanceObj.toString());
                similarity = 1.0 - distance;
            }

            // 🌟 로그에 실제 매칭된 질문 내용과 정확한 유사도 점수를 함께 출력
            log.info("⚡ [Semantic Cache HIT] 캐시 적중 완료!");
            log.info("🔍 캐시 분석 근거\n - 사용자 입력: '{}'\n - 매칭된 캐시 질문: '{}'\n - 계산된 유사도 점수: {} (커트라인: {})",
                    question, cachedDoc.getText(), String.format("%.4f", similarity), SEMANTIC_CACHE_THRESHOLD);

            // 캐시 적중 시에도 사용자의 채팅 기록에는 남겨줍니다.
            chatLogRepository.save(ChatLog.createNormal(userId, question, cachedAnswer));
            return new ChatbotAnswerResponse(cachedAnswer, true);
        }

        log.info("🐢 [Semantic Cache MISS] 캐시 없음. 정상적인 RAG 파이프라인을 실행합니다.");

        // 2. Vector DB 지식 검색
        String relevantKnowledge = knowledgeRetrievalPort.retrieveRelevantKnowledge(question);

        // 3. 지식 없음 처리 (도메인 외 질문 차단)
        if (relevantKnowledge == null || relevantKnowledge.isBlank()) {
            log.warn("[챗봇 응답] ❌ 서비스 무관 질문으로 판단. 필터링 처리: {}", question);
            String rejectMessage = "죄송합니다. 저는 알고가 서비스와 관련된 질문(결제, 환불, 코스 안내 등)에만 답변해 드릴 수 있어요.";
            chatLogRepository.save(ChatLog.createFiltered(userId, question, rejectMessage));
            return new ChatbotAnswerResponse(rejectMessage, false);
        }

        // 4. 정상 질문인 경우 이전 대화 기록 10개 조회 및 LLM 답변 생성
        List<ChatLog> chatHistory = chatLogRepository.findByUserId(userId, null, 10);
        String answer = mainLlmPort.generateAnswer(question, relevantKnowledge, chatHistory);

        // 5. 성공적으로 생성된 답변을 RDB(ChatLog)에 저장
        chatLogRepository.save(ChatLog.createNormal(userId, question, answer));

        // 🌟 6. [동적 캐시 저장 및 TTL 부여] 생성된 답변을 다음 사용자를 위해 캐시에 저장합니다.
        saveToSemanticCacheWithTtl(question, answer);

        return new ChatbotAnswerResponse(answer, true);
    }

    /**
     * Spring AI VectorStore에 데이터를 저장한 직후, Redis Native Key에 접근하여 TTL을 부여하는 로직
     */
    private void saveToSemanticCacheWithTtl(String question, String answer) {
        try {
            // 우리가 직접 Key(Document ID)를 명시적으로 생성합니다.
            String documentId = UUID.randomUUID().toString();
            Document cacheDoc = new Document(documentId, question, Map.of("cachedAnswer", answer));
            
            // 1. Vector DB 삽입 (텍스트 임베딩 변환 및 Redis HSET/JSON.SET 자동 수행)
            semanticCacheStore.add(List.of(cacheDoc));

            // 2. 방금 저장된 Redis Key를 추적하여 EXPIRE(만료 시간) 명령어 전송
            // SemanticCacheConfig 에서 설정한 prefix("algoga:semantic-cache:")와 결합됩니다.
            String redisKey = "algoga:semantic-cache:" + documentId;
            jedisPooled.expire(redisKey, CACHE_TTL_SECONDS);
            
            log.info("✅ [Semantic Cache 저장] 새로운 질문/답변 쌍 캐싱 완료 (Key: {}, TTL: {}초)", redisKey, CACHE_TTL_SECONDS);
        } catch (Exception e) {
            // 캐시 저장 실패가 메인 비즈니스(답변 반환)에 영향을 주지 않도록 예외를 삼킵니다.
            log.error("❌ [Semantic Cache 저장 실패] 캐시 저장 중 오류가 발생했습니다: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse askSuggestedQuestion(Long suggestedQuestionId) {
        SuggestedQuestion sq = suggestedQuestionRepository.findById(suggestedQuestionId)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorCode.SUGGESTED_QUESTION_NOT_FOUND));
        
        return new ChatbotAnswerResponse(sq.getAnswer(), true);
    }
}