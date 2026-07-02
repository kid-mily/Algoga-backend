package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorKnowledgeRetrievalAdapter implements KnowledgeRetrievalPort {

    private final VectorStore vectorStore;
    private static final double RAG_THRESHOLD = 0.85;

    @Override
    public String retrieveRelevantKnowledge(String question) {
        log.info("[Vector DB] '{}' 질문에 대한 지식 검색 및 필터링 동시 수행...", question);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(1)
                .build();

        List<Document> results;
        try {
            // DB 통신 중 예외 발생 가능성이 있는 부분
            results = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("[Vector DB 통신 에러] ❌ DB 연결 또는 검색 중 오류 발생. 안전하게 빈 값을 반환합니다: {}", e.getMessage());
            return ""; // 빈 값 반환 시 서비스 레이어에서 도메인 외 질문으로 취급하여 시스템 장애 방지
        }

        if (results.isEmpty()) {
            log.warn("[Vector DB 차단] ❌ DB에 저장된 예상 질문 데이터가 단 하나도 없습니다.");
            return "";
        }

        Document bestMatch = results.get(0);
        Object distanceObj = bestMatch.getMetadata().get("distance");

        if (distanceObj != null) {
            double distance = Double.parseDouble(distanceObj.toString());
            double similarity = 1.0 - distance;

            log.info("[Vector DB 매칭 로그] 🔍 분석 근거\n - 사용자 입력: '{}'\n - 가장 유사한 DB 데이터: '{}'\n - 유사도 점수: {}", 
                    question, bestMatch.getText(), similarity);

            if (similarity < RAG_THRESHOLD) {
                log.warn("[Vector DB 차단] ❌ 유사도 미달 ({}점 < 커트라인 {}점) -> 엉뚱한 질문으로 판단하여 차단함.", similarity, RAG_THRESHOLD);
                return ""; 
            }
        } else {
            log.warn("[Vector DB 경고] 매칭은 되었으나 distance 점수를 추출할 수 없습니다. 문서: {}", bestMatch.getText());
        }

        Object answerObj = bestMatch.getMetadata().get("answer");
        if (answerObj == null) {
            log.warn("[Vector DB 에러] ❌ 매칭은 성공했으나 해당 질문에 연결된 약관(answer) 데이터가 없습니다.");
            return "";
        }

        String bestAnswer = String.valueOf(answerObj);
        log.info("[Vector DB 통과] ✅ 커트라인 통과! 관련 약관 발견 -> 내용: {}", bestAnswer);
        
        return bestAnswer;
    }
}