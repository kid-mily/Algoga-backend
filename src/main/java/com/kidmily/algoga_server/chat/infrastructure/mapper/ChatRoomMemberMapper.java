package com.kidmily.algoga_server.chat.infrastructure.mapper;

import com.kidmily.algoga_server.chat.domain.model.ChatRoomMember;
import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomMemberJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatRoomMemberMapper {

    default ChatRoomMemberJpaEntity toJpaEntity(ChatRoomMember member) {
        if (member == null) return null;
        return ChatRoomMemberJpaEntity.builder()
                .roomId(member.getRoomId())
                .userId(member.getUserId())
                .build();
    }

    default ChatRoomMember toDomain(ChatRoomMemberJpaEntity entity) {
        if (entity == null) return null;
        return ChatRoomMember.reconstitute(
                entity.getId(),
                entity.getRoomId(),
                entity.getUserId(),
                entity.getJoinedAt()
        );
    }
}