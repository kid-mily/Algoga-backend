package com.kidmily.algoga_server.chat.infrastructure.mapper;

import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatMessageJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageMapper {

    default ChatMessageJpaEntity toJpaEntity(ChatMessage message) {
        if (message == null) return null;
        return ChatMessageJpaEntity.builder()
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .build();
    }

    default ChatMessage toDomain(ChatMessageJpaEntity entity) {
        if (entity == null) return null;
        return ChatMessage.reconstitute(
                entity.getId(),
                entity.getRoomId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}