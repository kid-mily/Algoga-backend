package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.Knowledge;
import com.kidmily.algoga_server.chatbot.domain.repository.KnowledgeRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.KnowledgeMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KnowledgeRepositoryAdapter implements KnowledgeRepository {

    private final JpaKnowledgeRepository repo;
    private final KnowledgeMapper mapper;

    @Override
    public Knowledge save(Knowledge domain) {
        return mapper.toDomain(repo.save(mapper.toJpaEntity(domain)));
    }

    @Override
    public Optional<Knowledge> findById(Long knowledgeId) {
        return repo.findById(knowledgeId).map(mapper::toDomain);
    }

    @Override
    public List<Knowledge> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long knowledgeId) {
        repo.deleteById(knowledgeId);
    }
}