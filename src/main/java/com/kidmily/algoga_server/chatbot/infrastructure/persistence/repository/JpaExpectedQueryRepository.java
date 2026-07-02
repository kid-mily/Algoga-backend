package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ExpectedQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaExpectedQueryRepository extends JpaRepository<ExpectedQueryEntity, Long> {
    List<ExpectedQueryEntity> findAllByKnowledgeId(Long knowledgeId);
    List<ExpectedQueryEntity> findByQueryTextContaining(String keyword);
}