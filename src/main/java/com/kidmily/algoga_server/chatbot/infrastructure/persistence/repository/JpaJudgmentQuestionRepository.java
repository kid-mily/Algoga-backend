package com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository;

import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.JudgmentQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaJudgmentQuestionRepository extends JpaRepository<JudgmentQuestionEntity, Long> {
}