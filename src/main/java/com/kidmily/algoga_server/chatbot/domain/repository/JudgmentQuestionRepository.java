// chatbot/domain/repository/JudgmentQuestionRepository.java
package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import java.util.List;
import java.util.Optional;

public interface JudgmentQuestionRepository {
    JudgmentQuestion save(JudgmentQuestion judgmentQuestion);
    
    // 🌟 조회 및 삭제 포트 추가
    Optional<JudgmentQuestion> findById(Long judgmentQuestionId);
    List<JudgmentQuestion> findAll();
    void deleteById(Long judgmentQuestionId);
}