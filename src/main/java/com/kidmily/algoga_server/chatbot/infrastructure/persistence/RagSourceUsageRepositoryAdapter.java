package com.kidmily.algoga_server.chatbot.infrastructure.persistence;

import com.kidmily.algoga_server.chatbot.domain.model.RagSourceUsage;
import com.kidmily.algoga_server.chatbot.domain.repository.RagSourceUsageRepository;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.RagSourceUsageEntity;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.repository.JpaRagSourceUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RagSourceUsageRepositoryAdapter implements RagSourceUsageRepository {

    private final JpaRagSourceUsageRepository jpaRepository;

    @Override
    public void saveAll(List<RagSourceUsage> usages) {
        if (usages == null || usages.isEmpty()) {
            return;
        }
        List<RagSourceUsageEntity> entities = usages.stream()
                .map(u -> RagSourceUsageEntity.builder()
                        .chatLogId(u.getChatLogId())
                        .source(u.getSource())
                        .page(u.getPage())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
        jpaRepository.saveAll(entities);
    }

    @Override
    public List<SourceStat> aggregate(Instant from, Instant toExclusive) {
        return jpaRepository.aggregate(from, toExclusive).stream()
                .map(p -> new SourceStat(p.getSource(), p.getPage(), p.getCount()))
                .toList();
    }
}
