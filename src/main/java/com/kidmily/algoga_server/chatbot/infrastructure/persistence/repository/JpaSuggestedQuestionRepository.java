package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.SuggestedQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSuggestedQuestionRepository extends JpaRepository<SuggestedQuestionEntity, Long> {
}