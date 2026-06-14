package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import java.util.List;
import java.util.Optional;

public interface JudgmentQuestionRepository {
    JudgmentQuestion save(JudgmentQuestion judgmentQuestion);
    Optional<JudgmentQuestion> findById(Long judgmentQuestionId);
    List<JudgmentQuestion> findAll();
    void deleteById(Long judgmentQuestionId);
}