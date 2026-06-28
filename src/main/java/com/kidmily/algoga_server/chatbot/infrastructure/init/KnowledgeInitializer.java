package com.kidmily.algoga_server.chatbot.infrastructure.init;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ExpectedQueryEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.KnowledgeEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaExpectedQueryRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeInitializer {

    private final JpaKnowledgeRepository knowledgeRepository;
    private final JpaExpectedQueryRepository expectedQueryRepository;
    private final VectorStore vectorStore;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeKnowledgeOnStartup() {
        log.info("[RAG 지식 동기화] 🚀 DB 데이터를 읽어 Redis 캐시를 최신 상태로 덮어씌웁니다...");

        List<KnowledgeEntity> knowledges = knowledgeRepository.findAll();
        List<ExpectedQueryEntity> expectedQueries = expectedQueryRepository.findAll();

        if (knowledges.isEmpty() || expectedQueries.isEmpty()) {
            log.warn("[RAG 지식 동기화] DB에 등록된 지식이나 예상 질문이 없습니다.");
            return;
        }

        // 1. 기존에 Redis에 있을지도 모르는 데이터 삭제
        List<String> documentIdsToDelete = expectedQueries.stream()
                .map(query -> "knowledge_" + query.getExpectedQueryId())
                .collect(Collectors.toList());
        vectorStore.delete(documentIdsToDelete);

        // 2. 약관 ID(PK)를 기준으로 약관 내용 매핑
        Map<Long, String> knowledgeContentMap = knowledges.stream()
                .collect(Collectors.toMap(KnowledgeEntity::getKnowledgeId, KnowledgeEntity::getContent));

        // 3. Document 객체로 변환 (타입 이슈 방지를 위해 String 캐스팅)
        List<Document> documents = expectedQueries.stream()
                .filter(query -> knowledgeContentMap.containsKey(query.getKnowledgeId()))
                .map(query -> new Document(
                        "knowledge_" + query.getExpectedQueryId(),
                        query.getQueryText(),
                        Map.of(
                                "knowledgeId", String.valueOf(query.getKnowledgeId()),
                                "answer", knowledgeContentMap.get(query.getKnowledgeId())
                        )
                ))
                .collect(Collectors.toList());

        // 4. Redis Vector DB에 일괄 저장
        vectorStore.add(documents);

        log.info("[RAG 지 동기화 완료] ✅ 총 {}개의 정책 기반 예상 질문이 Redis Vector DB에 최신화되었습니다.", documents.size());
    }
}