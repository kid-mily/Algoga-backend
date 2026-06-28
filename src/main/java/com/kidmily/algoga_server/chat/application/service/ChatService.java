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
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomMemberResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomResponse;
import com.kidmily.algoga_server.chat.settings.cache.ChatCacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

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
                .map(room -> ChatRoomResponse.of(room, partnerNickname, partnerProfile, null, null, 0, 2))
                .orElseGet(() -> {
                    ChatRoom newRoom = createNewRoom(command);
                    // 새 방 생성 시 양쪽 유저 캐시 무효화
                    evictChatRoomsCache(command.requesterId());
                    evictChatRoomsCache(command.targetUserId());
                    return ChatRoomResponse.of(newRoom, partnerNickname, partnerProfile, null, null, 0, 2);
                });
    }

    @Override
    @Cacheable(cacheNames = ChatCacheType.Const.CHAT_ROOMS, key = "#userId")
    //@Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Long userId) {
        log.info("[ChatService] 채팅방 목록 조회 - userId: {}", userId);

        return chatRoomMemberRepository.findByUserId(userId).stream()
                .map(member -> chatRoomRepository.findById(member.getRoomId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room -> {
                    Optional<ChatMessage> lastMsg = chatMessageRepository.findLastByRoomId(room.getId());
                    int unreadCount = (int) chatMessageReadRepository.countUnreadByRoomIdAndUserId(room.getId(), userId);
                    int memberCount = (int) chatRoomMemberRepository.countByRoomId(room.getId());

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
                            unreadCount,
                            memberCount
                    );
                })
                .collect(java.util.stream.Collectors.toList());
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

        chatRoomMemberRepository.findByRoomId(command.roomId())
                .forEach(member -> evictChatRoomsCache(member.getUserId()));

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
        evictChatRoomsCache(userId);
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, Long userId) {
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        chatMessageReadRepository.markAllAsRead(roomId, userId);
        evictChatRoomsCache(userId);

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

        evictChatRoomsCache(command.requesterId());
        command.targetUserIds().forEach(this::evictChatRoomsCache);

        return ChatRoomResponse.of(chatRoom, command.roomName(), null, null, null, 0, command.targetUserIds().size() + 1);
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

        evictChatRoomsCache(userId);

        reads.forEach(read -> evictChatRoomsCache(read.getUserId()));

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

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomMemberResponse> getRoomMembers(Long roomId, Long userId) {
        // 요청자가 해당 방의 멤버인지 검증
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        return chatRoomMemberRepository.findByRoomId(roomId).stream()
                .map(member -> {
                    Long memberId = member.getUserId();
                    return ChatRoomMemberResponse.of(
                            memberId,
                            userPort.getNickname(memberId),
                            userPort.getProfileImageUrl(memberId)
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public ChatRoomResponse addMembers(Long roomId, Long requesterId, List<Long> targetUserIds) {
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, requesterId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        targetUserIds.forEach(targetUserId ->
                userPort.findUserIdById(targetUserId)
                        .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_USER_NOT_FOUND))
        );

        if (room.getType() == ChatRoomType.GROUP) {
            ChatRoomResponse response = addToExistingGroup(room, targetUserIds);
            evictChatRoomsCache(requesterId);
            targetUserIds.forEach(this::evictChatRoomsCache);
            return response;
        } else {
            ChatRoomResponse response = createGroupFromDirect(roomId, requesterId, targetUserIds);
            evictChatRoomsCache(requesterId);
            targetUserIds.forEach(this::evictChatRoomsCache);
            return response;
        }
    }

    /** 그룹방에 멤버 추가 — 같은 방 유지 */
    private ChatRoomResponse addToExistingGroup(ChatRoom room, List<Long> targetUserIds) {
        // 현재 인원 + 신규 추가 인원이 최대치를 넘는지 도메인에서 검증
        long currentCount = chatRoomMemberRepository.countByRoomId(room.getId());
        long newCount = targetUserIds.stream()
                .distinct()
                .filter(id -> !chatRoomMemberRepository.existsByRoomIdAndUserId(room.getId(), id))
                .count();
        room.validateCanAddMembers((int) currentCount, (int) newCount);

        for (Long targetUserId : targetUserIds) {
            if (!chatRoomMemberRepository.existsByRoomIdAndUserId(room.getId(), targetUserId)) {
                chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), targetUserId));
            }
        }
        int memberCount = (int) chatRoomMemberRepository.countByRoomId(room.getId());
        return ChatRoomResponse.of(room, room.getRoomName(), null, null, null, 0, memberCount);
    }



    /** 1:1방에서 멤버 추가 — 원본은 그대로 두고 새 그룹방 생성 */
    private ChatRoomResponse createGroupFromDirect(Long directRoomId, Long requesterId, List<Long> targetUserIds) {
        // 기존 1:1방 멤버(나 + 상대) + 새로 추가할 멤버 = 합집합 (중복 제거)
        java.util.LinkedHashSet<Long> allMemberIds = new java.util.LinkedHashSet<>();
        chatRoomMemberRepository.findByRoomId(directRoomId)
                .forEach(m -> allMemberIds.add(m.getUserId()));
        allMemberIds.addAll(targetUserIds);

        String groupName = generateGroupName(allMemberIds);

        ChatRoom newRoom = chatRoomRepository.save(
                ChatRoom.create(ChatRoomType.GROUP, groupName, allMemberIds.size())
        );
        allMemberIds.forEach(memberId ->
                chatRoomMemberRepository.save(ChatRoomMember.create(newRoom.getId(), memberId))
        );

        return ChatRoomResponse.of(newRoom, groupName, null, null, null, 0, allMemberIds.size());
    }

    private String generateGroupName(java.util.Collection<Long> memberIds) {
        String joined = memberIds.stream()
                .map(userPort::getNickname)
                .collect(java.util.stream.Collectors.joining(", "));
        return joined.length() > 20 ? joined.substring(0, 20) : joined;
    }

    @Override
    @Transactional
    public void renameRoom(Long roomId, Long requesterId, String roomName) {
        // 요청자가 방 멤버인지 검증
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, requesterId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_NOT_MEMBER));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 1:1방은 이름 변경 불가 (상대 닉네임이 동적 표시되므로)
        if (room.getType() == ChatRoomType.DIRECT) {
            throw new ChatException(ChatErrorCode.CHAT_DIRECT_RENAME_NOT_ALLOWED);
        }

        chatRoomRepository.updateRoomName(roomId, roomName);

        chatRoomMemberRepository.findByRoomId(roomId)
                .forEach(member -> evictChatRoomsCache(member.getUserId()));
    }

    // 공통 헬퍼
    private void evictChatRoomsCache(Long userId) {
        Cache cache = cacheManager.getCache(ChatCacheType.Const.CHAT_ROOMS);
        if (cache != null) {
            cache.evict(userId);
        }
    }


}