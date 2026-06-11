package com.kidmily.algoga_server.chatbot.infrastructure.mapper;

import com.kidmily.algoga_server.chatbot.domain.model.JudgmentQuestion;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.JudgmentQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface JudgmentQuestionMapper {
    JudgmentQuestionEntity toJpaEntity(JudgmentQuestion domain);

    default JudgmentQuestion toDomain(JudgmentQuestionEntity entity) {
        if (entity == null) return null;
        return JudgmentQuestion.reconstitute(
                entity.getJudgmentQuestionId(),
                entity.getManagerId(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getCreatedAt()
        );
    }
}