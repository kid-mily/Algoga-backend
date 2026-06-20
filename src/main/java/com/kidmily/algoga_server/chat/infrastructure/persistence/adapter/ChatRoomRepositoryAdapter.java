package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomType;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomRepository;
import com.kidmily.algoga_server.chat.infrastructure.mapper.ChatRoomMapper;
import com.kidmily.algoga_server.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final SpringDataChatRoomRepository springDataRepository;
    private final ChatRoomMapper chatRoomMapper;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomMapper.toDomain(
                springDataRepository.save(chatRoomMapper.toJpaEntity(chatRoom))
        );
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return springDataRepository.findById(id).map(chatRoomMapper::toDomain);
    }

    @Override
    public void softDelete(Long roomId) {
        springDataRepository.softDeleteById(roomId);
    }


}