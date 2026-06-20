package com.kidmily.algoga_server.chat.infrastructure.mapper;

import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatRoomMapper {

    default ChatRoomJpaEntity toJpaEntity(ChatRoom chatRoom) {
        if (chatRoom == null) return null;
        return ChatRoomJpaEntity.builder()
                .type(chatRoom.getType())
                .roomName(chatRoom.getRoomName())
                .build();
    }

    default ChatRoom toDomain(ChatRoomJpaEntity entity) {
        if (entity == null) return null;
        return ChatRoom.reconstitute(entity.getId(), entity.getType(), entity.getCreatedAt(), entity.isDeleted(), entity.getRoomName());
    }
}