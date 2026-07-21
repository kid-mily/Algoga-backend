package com.kidmily.algoga_server.chat.application.service;

import com.kidmily.algoga_server.chat.application.command.SendChatMessageCommand;
import com.kidmily.algoga_server.chat.application.port.UserPort;
import com.kidmily.algoga_server.chat.domain.model.ChatMessage;
import com.kidmily.algoga_server.chat.domain.model.ChatRoom;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomMember;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomType;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageReadRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatMessageRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomMemberRepository;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomRepository;
import com.kidmily.algoga_server.chat.exception.ChatErrorCode;
import com.kidmily.algoga_server.chat.exception.ChatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatMessageReadRepository chatMessageReadRepository;

    @Mock
    private UserPort userPort;

    @Mock
    private CacheManager cacheManager;

    private static final Long ROOM_ID = 10L;
    private static final Long SENDER_ID = 1L;
    private static final Long OTHER_ID = 2L;

    private ChatRoom aliveRoom() {
        return ChatRoom.reconstitute(ROOM_ID, ChatRoomType.DIRECT, LocalDateTime.now(), false, "상대방");
    }

    // ===== sendMessage =====

    @Test
    @DisplayName("살아있는 방에 메시지를 보내면 메시지를 저장한다.")
    void sendMessage_success() {
        // given
        SendChatMessageCommand command = new SendChatMessageCommand(ROOM_ID, SENDER_ID, "안녕");

        given(chatRoomRepository.findById(ROOM_ID)).willReturn(Optional.of(aliveRoom()));
        given(chatRoomMemberRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                .willReturn(Optional.of(ChatRoomMember.create(ROOM_ID, SENDER_ID)));
        given(chatMessageRepository.save(any(ChatMessage.class)))
                .willReturn(ChatMessage.create(ROOM_ID, SENDER_ID, "안녕"));
        given(chatRoomMemberRepository.findByRoomId(ROOM_ID))
                .willReturn(List.of(
                        ChatRoomMember.create(ROOM_ID, SENDER_ID),
                        ChatRoomMember.create(ROOM_ID, OTHER_ID)
                ));
        lenient().when(userPort.getNickname(anyLong())).thenReturn("보낸사람");
        lenient().when(userPort.getProfileImageUrl(anyLong())).thenReturn(null);

        // when
        chatService.sendMessage(command);

        // then
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("삭제된(차단된) 방에 메시지를 보내면 CHAT_ROOM_NOT_FOUND 예외가 발생한다.")
    void sendMessage_deletedRoom_throws() {
        // given
        SendChatMessageCommand command = new SendChatMessageCommand(ROOM_ID, SENDER_ID, "안녕");
        // @SQLRestriction으로 삭제된 방은 findById가 empty 반환
        given(chatRoomRepository.findById(ROOM_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.sendMessage(command))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());

        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("멤버가 아닌 사용자가 메시지를 보내면 CHAT_NOT_MEMBER 예외가 발생한다.")
    void sendMessage_notMember_throws() {
        // given
        SendChatMessageCommand command = new SendChatMessageCommand(ROOM_ID, SENDER_ID, "안녕");
        given(chatRoomRepository.findById(ROOM_ID)).willReturn(Optional.of(aliveRoom()));
        given(chatRoomMemberRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.sendMessage(command))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_NOT_MEMBER.getMessage());

        verify(chatMessageRepository, never()).save(any());
    }

    // ===== getMessages / markAsRead 방 생존 검증 =====

    @Test
    @DisplayName("삭제된 방의 메시지를 조회하면 CHAT_ROOM_NOT_FOUND 예외가 발생한다.")
    void getMessages_deletedRoom_throws() {
        // given
        given(chatRoomRepository.findById(ROOM_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.getMessages(ROOM_ID, SENDER_ID))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("삭제된 방을 읽음 처리하면 CHAT_ROOM_NOT_FOUND 예외가 발생한다.")
    void markAsRead_deletedRoom_throws() {
        // given
        given(chatRoomRepository.findById(ROOM_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.markAsRead(ROOM_ID, SENDER_ID))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());

        verify(chatMessageReadRepository, never()).markAllAsRead(anyLong(), anyLong());
    }

    // ===== softDeleteDirectRoom (차단) =====

    @Test
    @DisplayName("차단 시 두 사람의 1:1 방이 존재하면 소프트 딜리트한다.")
    void softDeleteDirectRoom_success() {
        // given
        given(chatRoomMemberRepository.findDirectRoomIdByUserIds(SENDER_ID, OTHER_ID))
                .willReturn(Optional.of(ROOM_ID));

        // when
        chatService.softDeleteDirectRoom(SENDER_ID, OTHER_ID);

        // then
        verify(chatRoomRepository, times(1)).softDelete(ROOM_ID);
    }

    @Test
    @DisplayName("차단 시 두 사람의 1:1 방이 없으면 아무 작업도 하지 않는다.")
    void softDeleteDirectRoom_noRoom_doNothing() {
        // given
        given(chatRoomMemberRepository.findDirectRoomIdByUserIds(SENDER_ID, OTHER_ID))
                .willReturn(Optional.empty());

        // when
        chatService.softDeleteDirectRoom(SENDER_ID, OTHER_ID);

        // then
        verify(chatRoomRepository, never()).softDelete(anyLong());
    }

    // ===== leaveRoom =====

    @Test
    @DisplayName("마지막 멤버가 나가면 방을 소프트 딜리트하고 빈 결과를 반환한다.")
    void leaveRoom_lastMember_softDeletesRoom() {
        // given
        given(chatRoomMemberRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                .willReturn(Optional.of(ChatRoomMember.create(ROOM_ID, SENDER_ID)));
        given(userPort.getNickname(SENDER_ID)).willReturn("나가는사람");
        given(chatRoomMemberRepository.countByRoomId(ROOM_ID)).willReturn(0L);

        // when
        Optional<?> result = chatService.leaveRoom(ROOM_ID, SENDER_ID);

        // then
        verify(chatRoomMemberRepository, times(1)).deleteByRoomIdAndUserId(ROOM_ID, SENDER_ID);
        verify(chatRoomRepository, times(1)).softDelete(ROOM_ID);
        verify(chatMessageRepository, never()).save(any());   // 시스템 메시지 저장 안 함
        org.assertj.core.api.Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("남은 멤버가 있으면 방을 삭제하지 않고 시스템 메시지를 저장한다.")
    void leaveRoom_remainingMembers_savesSystemMessage() {
        // given
        given(chatRoomMemberRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                .willReturn(Optional.of(ChatRoomMember.create(ROOM_ID, SENDER_ID)));
        given(userPort.getNickname(SENDER_ID)).willReturn("나가는사람");
        given(chatRoomMemberRepository.countByRoomId(ROOM_ID)).willReturn(1L);
        given(chatMessageRepository.save(any(ChatMessage.class)))
                .willReturn(ChatMessage.create(ROOM_ID, 0L, "나가는사람님이 나갔습니다."));
        given(chatRoomMemberRepository.findByRoomId(ROOM_ID))
                .willReturn(List.of(ChatRoomMember.create(ROOM_ID, OTHER_ID)));

        // when
        chatService.leaveRoom(ROOM_ID, SENDER_ID);

        // then
        verify(chatRoomRepository, never()).softDelete(anyLong());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("멤버가 아닌 사용자가 방을 나가려 하면 CHAT_NOT_MEMBER 예외가 발생한다.")
    void leaveRoom_notMember_throws() {
        // given
        given(chatRoomMemberRepository.findByRoomIdAndUserId(ROOM_ID, SENDER_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.leaveRoom(ROOM_ID, SENDER_ID))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_NOT_MEMBER.getMessage());
    }
}