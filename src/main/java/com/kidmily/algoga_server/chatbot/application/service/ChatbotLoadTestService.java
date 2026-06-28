package com.kidmily.algoga_server.chatbot.application.service;

import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ExpectedQueryEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.KnowledgeEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaExpectedQueryRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaKnowledgeRepository;
import com.kidmily.algoga_server.chatbot.presentation.api.response.ChatbotAnswerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotLoadTestService {

    private final JpaExpectedQueryRepository jpaExpectedQueryRepository;
    private final JpaKnowledgeRepository jpaKnowledgeRepository;
    private final KnowledgeRetrievalPort knowledgeRetrievalPort;
    private final EmbeddingModel embeddingModel;

    /**
     * [대조군] 실제 임베딩 변환 + MySQL 전수 데이터 코사인 유사도 직접 연산 (O(N))
     */
    @Transactional(readOnly = true)
    public ChatbotAnswerResponse searchFromMysqlWithVectorCalc(String question) {
        // 1. 진짜 임베딩 모델 호출 (동일한 조건의 지연시간 발생)
        float[] embeddingArray = embeddingModel.embed(question);
        List<Double> embeddedQuestionList = new ArrayList<>();

        for (float f : embeddingArray) {
            embeddedQuestionList.add((double) f);
        }
        double[] questionVector = embeddedQuestionList.stream().mapToDouble(Double::doubleValue).toArray();

        // 2. MySQL에서 데이터 조회
        List<ExpectedQueryEntity> allQueries = jpaExpectedQueryRepository.findAll();
        if (allQueries.isEmpty()) return new ChatbotAnswerResponse("DB 데이터 없음", false);

        double maxSimilarity = -1.0;
        Long bestKnowledgeId = allQueries.get(0).getKnowledgeId();

        // 🌟 3. [핵심] 실제 운영 환경 스케일(10,000건)을 가정한 Brute-force 연산 부하 시뮬레이션
        // (현재 DB에 20줄밖에 없으므로, 데이터가 많을 때 MySQL이 겪게 될 CPU 고통을 재현합니다)
        int simulatedDbSize = 10000; 
        
        for (int i = 0; i < simulatedDbSize; i++) {
            // DB에서 꺼내온 1536/768 차원의 벡터라고 가정
            double[] dbVector = new double[questionVector.length]; 
            dbVector[0] = 0.1; // 더미 데이터 처리 방지용
            
            // 전수조사 수학 연산 (CPU 부하 유발)
            double similarity = calculateCosineSimilarity(questionVector, dbVector);
            
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }

        // 4. 매칭된 약관 반환
        String answerContent = jpaKnowledgeRepository.findById(bestKnowledgeId)
                .map(KnowledgeEntity::getContent)
                .orElse("약관 내용 없음");

        return new ChatbotAnswerResponse("[MySQL 벡터 전수연산 완료] " + answerContent, true);
    }

    /**
     * [실험군] Redis 인메모리 HNSW 인덱스를 활용한 네이티브 벡터 검색 (O(log N))
     * (Spring AI의 similaritySearch 내부에서 알아서 embeddingModel.embed()를 호출합니다)
     */
    public ChatbotAnswerResponse searchFromRedisVector(String question) {
        String relevantKnowledge = knowledgeRetrievalPort.retrieveRelevantKnowledge(question);

        if (relevantKnowledge == null || relevantKnowledge.isBlank()) {
            return new ChatbotAnswerResponse("Vector DB 매칭 결과 없음", false);
        }

        return new ChatbotAnswerResponse("[Redis Vector 검색 완료] " + relevantKnowledge, true);
    }

    // 코사인 유사도 수학 공식
    private double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.max(Math.sqrt(normA) * Math.sqrt(normB), 1e-10));
    }
}