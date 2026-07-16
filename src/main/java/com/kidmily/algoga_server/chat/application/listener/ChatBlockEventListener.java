package com.kidmily.algoga_server.chat.application.listener;

import com.kidmily.algoga_server.chat.application.usecase.ChatUseCase;
import com.kidmily.algoga_server.global.event.FriendBlockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBlockEventListener {

    private final ChatUseCase chatUseCase;

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
}