package com.kidmily.algoga_server.chat.application.service;

import com.kidmily.algoga_server.chat.application.command.CreateChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.CreateGroupChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.SendChatMessageCommand;
import com.kidmily.algoga_server.chat.application.port.UserPort;
import com.kidmily.algoga_server.chat.application.usecase.ChatUseCase;
import com.kidmily.algoga_server.chat.domain.model.*;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageReadRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomMemberRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomRepository;
import com.kidmily.algoga_server.chat.exception.ChatErrorCode;
import com.kidmily.algoga_server.chat.exception.ChatException;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatMessageResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;
    private final UserPort userPort;

    @Override
    @Transactional
    public ChatRoomResponse getOrCreateRoom(CreateChatRoomCommand command) {
        userPort.findUserIdById(command.targetUserId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_USER_NOT_FOUND));

        return chatRoomMemberRepository
                .findDirectRoomIdByUserIds(command.requesterId(), command.targetUserId())
                .flatMap(chatRoomRepository::findById)
                .map(room -> ChatRoomResponse.of(room, null, null, 0))
                .orElseGet(() -> ChatRoomResponse.of(createNewRoom(command), null, null, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Long userId) {
        log.info("[ChatService] 채팅방 목록 조회 - userId: {}", userId);

        return chatRoomMemberRepository.findByUserId(userId).stream()
                .map(member -> chatRoomRepository.findById(member.getRoomId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(room -> {
                    Optional<ChatMessage> lastMsg = chatMessageRepository.findLastByRoomId(room.getId());
                    int unreadCount = (int) chatMessageReadRepository.countUnreadByRoomIdAndUserId(room.getId(), userId);
                    return ChatRoomResponse.of(
                            room,
                            lastMsg.map(ChatMessage::getContent).orElse(null),
                            lastMsg.map(ChatMessage::getCreatedAt).orElse(null),
                            unreadCount
                    );
                })
                .toList();
    }

    private ChatRoom createNewRoom(CreateChatRoomCommand command) {
        String roomName = userPort.getNickname(command.targetUserId());
        ChatRoom newRoom = chatRoomRepository.save(ChatRoom.create(ChatRoomType.DIRECT, roomName, 1));
        chatRoomMemberRepository.save(ChatRoomMember.create(newRoom.getId(), command.requesterId()));
        chatRoomMemberRepository.save(ChatRoomMember.create(newRoom.getId(), command.targetUserId()));
        return newRoom;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(SendChatMessageCommand command) {
        chatRoomMemberRepository.findByRoomIdAndUserId(command.roomId(), command.senderId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.create(command.roomId(), command.senderId(), command.content())
        );

        // 발신자 제외 멤버 전원 미읽음 레코드 생성
        List<ChatMessageRead> reads = chatRoomMemberRepository.findByRoomId(command.roomId()).stream()
                .filter(member -> !member.getUserId().equals(command.senderId()))
                .map(member -> ChatMessageRead.create(message.getId(), member.getUserId()))
                .toList();
        chatMessageReadRepository.saveAll(reads);

        return message;
    }

    @Override
    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        chatMessageReadRepository.markAllAsRead(roomId, userId);
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, Long userId) {
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        chatMessageReadRepository.markAllAsRead(roomId, userId);

        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(message -> {
                    int unreadCount = (int) chatMessageReadRepository
                            .countUnreadByMessageId(message.getId());
                    return ChatMessageResponse.of(message, unreadCount);
                })
                .toList();
    }

    @Override
    @Transactional
    public ChatRoomResponse createGroupRoom(CreateGroupChatRoomCommand command) {
        log.info("[ChatService] 그룹 채팅방 개설 - requesterId: {}, targetUserIds: {}",
                command.requesterId(), command.targetUserIds());

        command.targetUserIds().forEach(targetUserId ->
                userPort.findUserIdById(targetUserId)
                        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_USER_NOT_FOUND))
        );

        ChatRoom chatRoom = chatRoomRepository.save(
                ChatRoom.create(ChatRoomType.GROUP, command.roomName(), command.targetUserIds().size())
        );
        chatRoomMemberRepository.save(ChatRoomMember.create(chatRoom.getId(), command.requesterId()));
        command.targetUserIds().forEach(targetUserId ->
                chatRoomMemberRepository.save(ChatRoomMember.create(chatRoom.getId(), targetUserId))
        );

        return ChatRoomResponse.of(chatRoom, null, null, 0);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        log.info("[ChatService] 채팅방 나가기 - roomId: {}, userId: {}", roomId, userId);

        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        chatRoomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
        chatMessageReadRepository.deleteByRoomIdAndUserId(roomId, userId);

        if (chatRoomMemberRepository.countByRoomId(roomId) == 0) {
            chatRoomRepository.softDelete(roomId);
        }
    }
}