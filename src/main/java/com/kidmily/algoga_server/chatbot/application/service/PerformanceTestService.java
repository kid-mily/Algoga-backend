package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PerformanceTestService {

    private final KnowledgeRetrievalPort knowledgeRetrievalPort;
    private final VectorStore semanticCacheStore;

    public PerformanceTestService(
            KnowledgeRetrievalPort knowledgeRetrievalPort,
            // @Lazy: 기동 시 semanticCacheStore(Ollama 연동)를 강제 초기화하지 않도록 지연 프록시 주입
            @Lazy @Qualifier("semanticCacheStore") VectorStore semanticCacheStore) {
        this.knowledgeRetrievalPort = knowledgeRetrievalPort;
        this.semanticCacheStore = semanticCacheStore;
    }

    // ==========================================
    // [실험 1] MySQL vs Redis 벡터 검색 성능 비교
    // ==========================================
    
    // [대조군 1] MySQL 벡터 풀스캔 시뮬레이션 (데이터 1만 건 가정)
    public String testMysqlVector(String question) {
        int simulatedDbSize = 10000;
        double dummyResult = 0.0;
        
        // 1만 번의 코사인 유사도 수학 연산 (CPU 부하 유발)
        for (int i = 0; i < simulatedDbSize; i++) {
            dummyResult += Math.random(); // 연산 부하 시뮬레이션
        }
        return "MySQL 연산 완료 (연산결과: " + dummyResult + ")";
    }

    // [실험군 1] Redis HNSW 인덱스 벡터 검색
    public String testRedisVector(String question) {
        // 실제 Redis Vector DB를 찌름
        return knowledgeRetrievalPort.retrieveRelevantKnowledge(question);
    }

    // ==========================================
    // [실험 2] 시맨틱 캐시 사용 vs 미사용 비교
    // ==========================================

    // [대조군 2] 시맨틱 캐시 미사용 (매번 검색 + LLM 1.5초 대기)
    public String testCacheOff(String question) {
        String knowledge = knowledgeRetrievalPort.retrieveRelevantKnowledge(question);
        simulateLlmGeneration(); // 무조건 1.5초 소요
        return "LLM 답변 생성 완료 (No Cache)";
    }

    // [실험군 2] 시맨틱 캐시 사용 (Hit 시 0.01초 컷)
    public String testCacheOn(String question) {
        // 1. 캐시 조회 (유사도 95% 이상)
        List<Document> cachedResults = semanticCacheStore.similaritySearch(
                SearchRequest.builder().query(question).topK(1).similarityThreshold(0.95).build()
        );

        if (!cachedResults.isEmpty()) {
            return "Cache HIT ⚡: " + cachedResults.get(0).getMetadata().get("cachedAnswer");
        }

        // 2. 캐시 Miss 시 LLM 호출(1.5초) 후 캐시에 저장
        String knowledge = knowledgeRetrievalPort.retrieveRelevantKnowledge(question);
        simulateLlmGeneration();
        
        String answer = "환불은 7일 이내 가능합니다."; // LLM이 생성한 답변이라 가정
        semanticCacheStore.add(List.of(new Document(question, Map.of("cachedAnswer", answer))));
        
        return "Cache MISS 🐢: LLM 답변 생성 완료";
    }

    // 외부 AI 모델(LLM) 텍스트 생성 시간 시뮬레이션 (평균 1.5초)
    private void simulateLlmGeneration() {
        try {
            Thread.sleep(1500); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}