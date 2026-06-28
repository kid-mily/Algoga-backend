package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.ExpectedQuery;
import java.util.List;
import java.util.Optional;

public interface ExpectedQueryRepository {
    ExpectedQuery save(ExpectedQuery expectedQuery);
    Optional<ExpectedQuery> findById(Long expectedQueryId);
    List<ExpectedQuery> findAllByKnowledgeId(Long knowledgeId);
    void deleteById(Long expectedQueryId);
}