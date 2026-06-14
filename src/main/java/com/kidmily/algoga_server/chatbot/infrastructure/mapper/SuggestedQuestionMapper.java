// chatbot/infrastructure/mapper/SuggestedQuestionMapper.java
package com.kidmily.algoga_server.chatbot.infrastructure.mapper;

import com.kidmily.algoga_server.chatbot.domain.model.SuggestedQuestion;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.SuggestedQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SuggestedQuestionMapper {
    
    // 🌟 추가됨: 도메인을 JPA 엔티티로 변환
    SuggestedQuestionEntity toJpaEntity(SuggestedQuestion domain);

    default SuggestedQuestion toDomain(SuggestedQuestionEntity entity) {
        if (entity == null) return null;
        return SuggestedQuestion.builder()
                .suggestedQuestionId(entity.getSuggestedQuestionId())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .build();
    }
}