package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.ExpectedQuery;
import com.kidmily.algoga_server.chatbot.domain.repository.ExpectedQueryRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.ExpectedQueryMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaExpectedQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExpectedQueryRepositoryAdapter implements ExpectedQueryRepository {

    private final JpaExpectedQueryRepository repo;
    private final ExpectedQueryMapper mapper;

    @Override
    public ExpectedQuery save(ExpectedQuery domain) {
        return mapper.toDomain(repo.save(mapper.toJpaEntity(domain)));
    }

    @Override
    public Optional<ExpectedQuery> findById(Long expectedQueryId) {
        return repo.findById(expectedQueryId).map(mapper::toDomain);
    }

    @Override
    public List<ExpectedQuery> findAllByKnowledgeId(Long knowledgeId) {
        return repo.findAllByKnowledgeId(knowledgeId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long expectedQueryId) {
        repo.deleteById(expectedQueryId);
    }
}