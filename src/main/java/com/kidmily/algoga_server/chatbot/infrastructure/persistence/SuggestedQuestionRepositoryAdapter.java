package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.SuggestedQuestionRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.SuggestedQuestionMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaSuggestedQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuggestedQuestionRepositoryAdapter implements SuggestedQuestionRepository {

    private final JpaSuggestedQuestionRepository repo;
    private final SuggestedQuestionMapper mapper;

    @Override
    public Optional<SuggestedQuestion> findById(Long suggestedQuestionId) {
        return repo.findById(suggestedQuestionId).map(mapper::toDomain);
    }

    @Override
    public List<SuggestedQuestion> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public SuggestedQuestion save(SuggestedQuestion domain) {
        return mapper.toDomain(repo.save(mapper.toJpaEntity(domain)));
    }

    @Override
    public void deleteById(Long suggestedQuestionId) {
        repo.deleteById(suggestedQuestionId);
    }
}