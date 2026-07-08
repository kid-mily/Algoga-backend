package com.kidmily.algoga_server.chatbot.infrastructure.llm;

import com.kidmily.algoga_server.chatbot.application.port.out.KnowledgeRetrievalPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VectorKnowledgeRetrievalAdapter implements KnowledgeRetrievalPort {

    // ObjectProvider로 주입받아 실제 사용 시점(getObject)에만 VectorStore를 생성한다.
    // 이렇게 해야 기동 시 이 어댑터가 만들어질 때 Lazy VectorStore를 강제로 즉시 초기화하지 않아,
    // Ollama/Redis가 없어도 서버 기동이 실패하지 않는다.
    // VectorStore 빈이 2개(vectorStore, semanticCacheStore)이므로 지식 검색용 빈을 Qualifier로 지정한다.
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private static final double RAG_THRESHOLD = 0.85;

    public VectorKnowledgeRetrievalAdapter(
            @Qualifier("vectorStore") ObjectProvider<VectorStore> vectorStoreProvider) {
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @Override
    public String retrieveRelevantKnowledge(String question) {
        log.info("[Vector DB] '{}' 질문에 대한 지식 검색 및 필터링 동시 수행...", question);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(1)
                .build();

        List<Document> results;
        try {
            // DB 통신 중 예외 발생 가능성이 있는 부분 (VectorStore Lazy 초기화도 이 시점에 수행됨)
            results = vectorStoreProvider.getObject().similaritySearch(searchRequest);
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