package com.kidmily.algoga_server.chat.infrastructure.persistence.adapter;

import com.kidmily.algoga_server.chat.application.port.ChatCleanupPort;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageReadRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatMessageRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomMemberRepository;
import com.kidmily.algoga_server.chat.infrastructure.persistence.repository.SpringDataChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatCleanupPortAdapter implements ChatCleanupPort {

    private final SpringDataChatRoomRepository chatRoomRepository;
    private final SpringDataChatMessageRepository chatMessageRepository;
    private final SpringDataChatRoomMemberRepository chatRoomMemberRepository;
    private final SpringDataChatMessageReadRepository chatMessageReadRepository;

    @Override
    public List<Long> findExpiredRoomIds(LocalDateTime threshold) {
        return chatRoomRepository.findExpiredRoomIds(threshold);
    }

    @Override
    public void hardDeleteRooms(List<Long> roomIds) {
        // 자식 → 부모 순서로 삭제
        // 읽음 기록이 message_id를 참조하므로 메시지보다 먼저 지워야 고아 데이터가 남지 않음
        chatMessageReadRepository.deleteByRoomIdIn(roomIds);   // ① 읽음 기록
        chatMessageRepository.deleteByRoomIdIn(roomIds);       // ② 메시지
        chatRoomMemberRepository.deleteByRoomIdIn(roomIds);    // ③ 멤버
        chatRoomRepository.hardDeleteByIds(roomIds);           // ④ 채팅방
    }
}