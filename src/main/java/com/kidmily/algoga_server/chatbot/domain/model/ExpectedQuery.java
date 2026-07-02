package com.kidmily.algoga_server.chatbot.domain.model;
import lombok.Builder; import lombok.Getter;
@Getter
public class ExpectedQuery {
    private final Long expectedQueryId; private final Long knowledgeId; private String queryText;
    @Builder private ExpectedQuery(Long expectedQueryId, Long knowledgeId, String queryText) {
        this.expectedQueryId = expectedQueryId; this.knowledgeId = knowledgeId; this.queryText = queryText;
    }
    public static ExpectedQuery create(Long knowledgeId, String queryText) {
        return ExpectedQuery.builder().knowledgeId(knowledgeId).queryText(queryText).build();
    }
}