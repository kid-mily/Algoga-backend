package com.kidmily.algoga_server.chat.infrastructure.mapper;

import com.kidmily.algoga_server.chat.domain.model.ChatMessageRead;
import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatMessageReadJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageReadMapper {

    default ChatMessageReadJpaEntity toJpaEntity(ChatMessageRead read) {
        if (read == null) return null;
        return ChatMessageReadJpaEntity.builder()
                .messageId(read.getMessageId())
                .userId(read.getUserId())
                .readAt(read.getReadAt())
                .build();
    }

    default ChatMessageRead toDomain(ChatMessageReadJpaEntity entity) {
        if (entity == null) return null;
        return ChatMessageRead.reconstitute(
                entity.getId(), entity.getMessageId(),
                entity.getUserId(), entity.getReadAt()
        );
    }
}