package com.kidmily.algoga_server.chatbot.infrastructure.mapper;

import com.kidmily.algoga_server.chatbot.domain.model.ChatLog;
import com.kidmily.algoga_server.chatbot.infrastructure.persistence.entity.ChatLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatLogMapper {
    ChatLogEntity toJpaEntity(ChatLog domain);

    default ChatLog toDomain(ChatLogEntity entity) {
        if (entity == null) return null;
        return ChatLog.reconstitute(
                entity.getChatLogId(),
                entity.getUserId(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.isFiltered(),
                entity.getCreatedAt()
        );
    }
}