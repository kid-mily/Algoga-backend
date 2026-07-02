package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.Knowledge;
import java.util.List;
import java.util.Optional;

public interface KnowledgeRepository {
    Knowledge save(Knowledge knowledge);
    Optional<Knowledge> findById(Long knowledgeId);
    List<Knowledge> findAll();
    void deleteById(Long knowledgeId);
}