package com.kidmily.algoga_server.chatbot.infrastructure.init;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ExpectedQueryEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.KnowledgeEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaExpectedQueryRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaKnowledgeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KnowledgeInitializer {

    private final JpaKnowledgeRepository knowledgeRepository;
    private final JpaExpectedQueryRepository expectedQueryRepository;
    // ObjectProvider로 주입: 기동 시 VectorStore(및 Ollama 연동)를 강제 초기화하지 않기 위함.
    // 실제 사용 시점(getObject)에만 초기화되며, 실패해도 아래 try/catch가 흡수한다.
    // VectorStore 빈이 2개이므로 지식 검색용 빈을 Qualifier로 지정한다.
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public KnowledgeInitializer(
            JpaKnowledgeRepository knowledgeRepository,
            JpaExpectedQueryRepository expectedQueryRepository,
            @Qualifier("vectorStore") ObjectProvider<VectorStore> vectorStoreProvider) {
        this.knowledgeRepository = knowledgeRepository;
        this.expectedQueryRepository = expectedQueryRepository;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeKnowledgeOnStartup() {
        try {log.info("[RAG 지식 동기화] 🚀 DB 데이터를 읽어 Redis 캐시를 최신 상태로 덮어씌웁니다...");

        // 이 시점에 VectorStore가 최초 초기화된다(Ollama/Redis 연동). 실패해도 catch에서 흡수한다.
        VectorStore vectorStore = vectorStoreProvider.getObject();

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
    } catch (Exception e) {
            // MySQL이나 Redis에 연결할 수 없어도 서버가 다운되지 않도록 예외를 잡아서 로깅만 처리
            log.error("[RAG 지식 동기화 실패] ❌ DB 연동 실패로 인해 지식 동기화를 건너뛰고 서버 실행을 계속합니다. 원인: {}", e.getMessage());
        }
    }
}