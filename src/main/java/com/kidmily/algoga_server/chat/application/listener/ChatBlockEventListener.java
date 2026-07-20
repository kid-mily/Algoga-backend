package com.kidmily.algoga_server.chat.application.listener;

import com.kidmily.algoga_server.chat.application.usecase.ChatUseCase;
import com.kidmily.algoga_server.chat.domain.model.ChatRoomMember;
import com.kidmily.algoga_server.chat.domain.repository.ChatRoomMemberRepository;
import com.kidmily.algoga_server.global.event.FriendBlockedEvent;
import com.kidmily.algoga_server.global.event.UserWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBlockEventListener {

    private final ChatUseCase chatUseCase;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFriendBlocked(FriendBlockedEvent event) {
        try {
            chatUseCase.softDeleteDirectRoom(event.blockerId(), event.blockedId());
            log.info("[ChatBlockEventListener] 차단으로 1:1 채팅방 삭제 - blocker: {}, blocked: {}",
                    event.blockerId(), event.blockedId());
        } catch (Exception e) {
            log.error("[ChatBlockEventListener] 차단 채팅방 삭제 실패 - {}", e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserWithdrawnEvent(UserWithdrawnEvent event) {
        Long withdrawnUserId = event.userId();
        log.info("[Chat] 유저 탈퇴 이벤트 수신 - userId: {}", withdrawnUserId);

        try {
            List<Long> roomIds = chatRoomMemberRepository.findByUserId(withdrawnUserId).stream()
                    .map(ChatRoomMember::getRoomId)
                    .toList();

            roomIds.forEach(roomId -> chatUseCase.leaveRoom(roomId, withdrawnUserId));

            log.info("[Chat] 유저({}) 채팅방 {}개 나가기 처리 완료", withdrawnUserId, roomIds.size());
        } catch (Exception e) {
            log.error("[Chat] 유저 탈퇴 채팅 데이터 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}