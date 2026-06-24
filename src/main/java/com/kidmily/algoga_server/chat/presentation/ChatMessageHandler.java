package com.kidmily.algoga_server.chat.presentation;

import com.kidmily.algoga_server.chat.application.command.SendChatMessageCommand;
import com.kidmily.algoga_server.chat.application.usecase.ChatUseCase;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final ChatUseCase chatUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트: STOMP SEND /app/chat/rooms/{roomId}/send
    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload SendMessagePayload payload,
                            SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");

        ChatMessageResponse response = chatUseCase.sendMessage(
                new SendChatMessageCommand(roomId, senderId, payload.content())
        );

        // 1) 방 구독자 전체에게 메시지 브로드캐스트 (기존)
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, response);

        // 2) 방 멤버 각자의 개인 채널로 목록 갱신 알림 (발신자 제외)
        chatUseCase.getRoomMemberIds(roomId).stream()
                .filter(memberId -> !memberId.equals(senderId))
                .forEach(memberId -> {
                    int unreadCount = chatUseCase.getUnreadCount(roomId, memberId);
                    log.info("[ChatHandler] 개인 알림 전송 - userId: {}, roomId: {}", memberId, roomId);
                    messagingTemplate.convertAndSend(
                            "/topic/users/" + memberId,
                            new RoomNotification(roomId, response.content(), response.createdAt(), unreadCount)
                    );
                });
    }

    // 클라이언트: STOMP SEND /app/chat/rooms/{roomId}/read
    @MessageMapping("/chat/rooms/{roomId}/read")
    public void markAsRead(@DestinationVariable Long roomId,
                           SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        chatUseCase.markAsRead(roomId, userId);

        // READ_EVENT 브로드캐스트 — 상대방 화면의 "1" 제거
        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + roomId + "/read",
                new ReadEventPayload(roomId, userId)
        );
    }

    public record SendMessagePayload(String content) {}
    public record ReadEventPayload(Long roomId, Long readerId) {}
    public record RoomNotification(Long roomId, String lastMessage,
                                   LocalDateTime lastMessageAt, int unreadCount) {}
}