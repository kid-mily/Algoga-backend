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

        // 해당 방 구독자 전체에게 브로드캐스트 (발신자 정보 + unreadCount 포함)
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, response);
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
}