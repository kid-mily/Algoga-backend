package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.domain.repository.JudgmentQuestionRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.mapper.JudgmentQuestionMapper;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaJudgmentQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JudgmentQuestionRepositoryAdapter implements JudgmentQuestionRepository {

    private final JpaJudgmentQuestionRepository repo;
    private final JudgmentQuestionMapper mapper;

    @Override
    public JudgmentQuestion save(JudgmentQuestion domain) {
        return mapper.toDomain(repo.save(mapper.toJpaEntity(domain)));
    }

    @Override
    public Optional<JudgmentQuestion> findById(Long judgmentQuestionId) {
        return repo.findById(judgmentQuestionId).map(mapper::toDomain);
    }

    @Override
    public List<JudgmentQuestion> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long judgmentQuestionId) {
        repo.deleteById(judgmentQuestionId);
    }
}