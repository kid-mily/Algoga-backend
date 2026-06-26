package com.kidmily.algoga_server.chat.presentation.api;

import com.kidmily.algoga_server.chat.application.command.CreateChatRoomCommand;
import com.kidmily.algoga_server.chat.application.command.CreateGroupChatRoomCommand;
import com.kidmily.algoga_server.chat.application.usecase.ChatUseCase;
import com.kidmily.algoga_server.chat.presentation.api.request.CreateChatRoomRequest;
import com.kidmily.algoga_server.chat.presentation.api.request.CreateGroupChatRoomRequest;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatMessageResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomMemberResponse;
import com.kidmily.algoga_server.chat.presentation.api.response.ChatRoomResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.kidmily.algoga_server.chat.presentation.ChatMessageHandler;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/rooms")
    @Operation(summary = "1:1 채팅방 개설/진입", description = "친구와의 1:1 채팅방을 개설하거나 기존 채팅방에 진입합니다.")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        Long currentUserId = userDetails.getUser().getId();

        ChatRoomResponse response = chatUseCase.getOrCreateRoom(
                new CreateChatRoomCommand(currentUserId, request.targetUserId())
        );

        return ResponseEntity.ok(ApiResponse.success("CHAT_ROOM_FOUND", "채팅방 진입에 성공했습니다.", response));
    }

    @GetMapping("/rooms")
    @Operation(summary = "채팅방 목록 조회", description = "내가 참여 중인 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        List<ChatRoomResponse> response = chatUseCase.getRooms(currentUserId);

        return ResponseEntity.ok(ApiResponse.success("CHAT_ROOMS_FOUND", "채팅방 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅 내역 조회", description = "채팅방의 과거 메시지를 조회합니다. 진입 시 자동으로 읽음 처리됩니다.")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        List<ChatMessageResponse> response = chatUseCase.getMessages(roomId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("CHAT_MESSAGES_FOUND", "채팅 내역 조회에 성공했습니다.", response));
    }

    @PostMapping("/rooms/group")
    @Operation(summary = "그룹 채팅방 개설", description = "여러 명을 초대하여 그룹 채팅방을 개설합니다.")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createGroupRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGroupChatRoomRequest request
    ) {
        Long currentUserId = userDetails.getUser().getId();

        ChatRoomResponse response = chatUseCase.createGroupRoom(
                new CreateGroupChatRoomCommand(currentUserId, request.targetUserIds(), request.roomName())
        );

        return ResponseEntity.ok(ApiResponse.success("CHAT_GROUP_ROOM_CREATED", "그룹 채팅방이 개설되었습니다.", response));
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방을 나갑니다. 마지막 멤버가 나가면 방이 삭제됩니다.")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        chatUseCase.leaveRoom(roomId, currentUserId).ifPresent(systemMessage -> {
            // 1) 방에 남은 구독자에게 시스템 메시지 실시간 전송 (DB에 저장된 메시지 그대로)
            messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, systemMessage);

            // 2) 남은 멤버 각자의 개인 채널로 목록 갱신 알림
            chatUseCase.getRoomMemberIds(roomId).forEach(memberId -> {
                int unreadCount = chatUseCase.getUnreadCount(roomId, memberId);
                messagingTemplate.convertAndSend(
                        "/topic/users/" + memberId,
                        new ChatMessageHandler.RoomNotification(
                                roomId, systemMessage.content(), systemMessage.createdAt(), unreadCount)
                );
            });
        });

        return ResponseEntity.ok(ApiResponse.success("CHAT_ROOM_LEFT", "채팅방을 나갔습니다.", null));
    }

    @GetMapping("/rooms/{roomId}/members")
    @Operation(summary = "채팅방 멤버 조회", description = "채팅방에 참여 중인 멤버 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChatRoomMemberResponse>>> getRoomMembers(
            @Parameter(description = "채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        List<ChatRoomMemberResponse> response = chatUseCase.getRoomMembers(roomId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("CHAT_ROOM_MEMBERS_FOUND", "채팅방 멤버 조회에 성공했습니다.", response));
    }
}