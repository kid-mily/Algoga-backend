package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.SuggestedQuestionEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaSuggestedQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuggestedQuestionRepositoryAdapter implements SuggestedQuestionRepository {

    private final JpaSuggestedQuestionRepository jpaRepository;

    @Override
    public Optional<SuggestedQuestion> findById(Long suggestedQuestionId) {
        return jpaRepository.findById(suggestedQuestionId)
                .map(this::toDomain);
    }

    @Override
    public List<SuggestedQuestion> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    // MapStruct를 사용해도 되고, 직접 매퍼 메서드를 작성해도 됩니다.
    private SuggestedQuestion toDomain(SuggestedQuestionEntity entity) {
        return SuggestedQuestion.builder()
                .suggestedQuestionId(entity.getSuggestedQuestionId())
                .managerId(entity.getManagerId())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}