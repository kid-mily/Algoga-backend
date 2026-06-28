package com.kidmily.algoga_server.chatbot.infrastructure.mapper;

import com.kidmily.algoga_server.chatbot.domain.model.ExpectedQuery;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ExpectedQueryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExpectedQueryMapper {
    ExpectedQueryEntity toJpaEntity(ExpectedQuery domain);

    default ExpectedQuery toDomain(ExpectedQueryEntity entity) {
        if (entity == null) return null;
        return ExpectedQuery.builder()
                .expectedQueryId(entity.getExpectedQueryId())
                .knowledgeId(entity.getKnowledgeId())
                .queryText(entity.getQueryText())
                .build();
    }
}