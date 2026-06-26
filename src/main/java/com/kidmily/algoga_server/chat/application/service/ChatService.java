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

    private static final long SYSTEM_SENDER_ID = 0L;

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

        String partnerNickname = userPort.getNickname(command.targetUserId());
        String partnerProfile = userPort.getProfileImageUrl(command.targetUserId());

        return chatRoomMemberRepository
                .findDirectRoomIdByUserIds(command.requesterId(), command.targetUserId())
                .flatMap(chatRoomRepository::findById)
                .map(room -> ChatRoomResponse.of(room, partnerNickname, partnerProfile, null, null, 0))
                .orElseGet(() -> ChatRoomResponse.of(createNewRoom(command), partnerNickname, partnerProfile, null, null, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Long userId) {
        log.info("[ChatService] 채팅방 목록 조회 - userId: {}", userId);

        return chatRoomMemberRepository.findByUserId(userId).stream()
                .map(member -> chatRoomRepository.findById(member.getRoomId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room -> {
                    Optional<ChatMessage> lastMsg = chatMessageRepository.findLastByRoomId(room.getId());
                    int unreadCount = (int) chatMessageReadRepository.countUnreadByRoomIdAndUserId(room.getId(), userId);

                    String displayName = room.getRoomName();
                    String profileImageUrl = null;

                    // 1:1 채팅방은 "나 아닌 상대방"의 닉네임/프로필을 동적으로 표시
                    if (room.getType() == ChatRoomType.DIRECT) {
                        Long partnerId = chatRoomMemberRepository.findByRoomId(room.getId()).stream()
                                .map(ChatRoomMember::getUserId)
                                .filter(id -> !id.equals(userId))
                                .findFirst()
                                .orElse(null);
                        if (partnerId != null) {
                            displayName = userPort.getNickname(partnerId);
                            profileImageUrl = userPort.getProfileImageUrl(partnerId);
                        }
                    }

                    return ChatRoomResponse.of(
                            room,
                            displayName,
                            profileImageUrl,
                            lastMsg.map(ChatMessage::getContent).orElse(null),
                            lastMsg.map(ChatMessage::getCreatedAt).orElse(null),
                            unreadCount
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendChatMessageCommand command) {
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

        // 방금 생성된 미읽음 레코드 수 = 안 읽은 사람 수 (실시간 브로드캐스트용 정확한 값)
        int unreadCount = reads.size();

        return ChatMessageResponse.of(
                message,
                userPort.getNickname(command.senderId()),
                userPort.getProfileImageUrl(command.senderId()),
                unreadCount
        );
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

        java.util.Map<Long, String> nameCache = new java.util.HashMap<>();
        java.util.Map<Long, String> profileCache = new java.util.HashMap<>();

        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(message -> {
                    Long sid = message.getSenderId();
                    String nickname = nameCache.computeIfAbsent(sid, userPort::getNickname);
                    String profile = profileCache.computeIfAbsent(sid, userPort::getProfileImageUrl);
                    int unreadCount = (int) chatMessageReadRepository.countUnreadByMessageId(message.getId());
                    return ChatMessageResponse.of(message, nickname, profile, unreadCount);
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

        return ChatRoomResponse.of(chatRoom, command.roomName(), null, null, null, 0);
    }

    @Override
    @Transactional
    public Optional<ChatMessageResponse> leaveRoom(Long roomId, Long userId) {
        log.info("[ChatService] 채팅방 나가기 - roomId: {}, userId: {}", roomId, userId);

        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        // 나가는 사람 닉네임은 멤버 삭제 전에 확보
        String leaverNickname = userPort.getNickname(userId);

        chatRoomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
        chatMessageReadRepository.deleteByRoomIdAndUserId(roomId, userId);

        // 마지막 멤버였으면 방 삭제 후 종료 (알릴 대상 없음)
        if (chatRoomMemberRepository.countByRoomId(roomId) == 0) {
            chatRoomRepository.softDelete(roomId);
            return Optional.empty();
        }

        // 시스템 메시지 저장 (senderId = 0 → 프론트가 시스템 메시지로 렌더)
        ChatMessage systemMessage = chatMessageRepository.save(
                ChatMessage.create(roomId, SYSTEM_SENDER_ID, leaverNickname + "님이 나갔습니다.")
        );

        // 남은 멤버 전원 미읽음 레코드 생성
        List<ChatMessageRead> reads = chatRoomMemberRepository.findByRoomId(roomId).stream()
                .map(member -> ChatMessageRead.create(systemMessage.getId(), member.getUserId()))
                .toList();
        chatMessageReadRepository.saveAll(reads);

        return Optional.of(ChatMessageResponse.of(systemMessage, "", null, reads.size()));
    }

    private ChatRoom createNewRoom(CreateChatRoomCommand command) {
        String roomName = userPort.getNickname(command.targetUserId());
        ChatRoom newRoom = chatRoomRepository.save(ChatRoom.create(ChatRoomType.DIRECT, roomName, 1));
        chatRoomMemberRepository.save(ChatRoomMember.create(newRoom.getId(), command.requesterId()));
        chatRoomMemberRepository.save(ChatRoomMember.create(newRoom.getId(), command.targetUserId()));
        return newRoom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRoomMemberIds(Long roomId) {
        return chatRoomMemberRepository.findByRoomId(roomId).stream()
                .map(ChatRoomMember::getUserId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long roomId, Long userId) {
        return (int) chatMessageReadRepository.countUnreadByRoomIdAndUserId(roomId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getNickname(Long userId) {
        return userPort.getNickname(userId);
    }
}