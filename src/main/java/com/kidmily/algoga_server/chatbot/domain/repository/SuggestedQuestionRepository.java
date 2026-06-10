package com.kidmily.algoga_server.chatbot.domain.repository;

import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import java.util.List;
import java.util.Optional;

public interface SuggestedQuestionRepository {
    Optional<SuggestedQuestion> findById(Long suggestedQuestionId);
    List<SuggestedQuestion> findAll();
}