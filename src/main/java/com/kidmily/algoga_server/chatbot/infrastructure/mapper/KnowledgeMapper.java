package com.kidmily.algoga_server.chatbot.infrastructure.mapper;

import com.kidmily.algoga_server.chatbot.domain.model.Knowledge;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.KnowledgeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnowledgeMapper {
    KnowledgeEntity toJpaEntity(Knowledge domain);

    default Knowledge toDomain(KnowledgeEntity entity) {
        if (entity == null) return null;
        return Knowledge.builder()
                .knowledgeId(entity.getKnowledgeId())
                .managerId(entity.getManagerId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}