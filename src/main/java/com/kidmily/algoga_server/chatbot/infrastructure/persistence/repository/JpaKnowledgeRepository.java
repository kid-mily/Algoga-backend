package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.KnowledgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaKnowledgeRepository extends JpaRepository<KnowledgeEntity, Long> {
}