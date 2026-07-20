package com.kidmily.algoga_server.chatbot.presentation.api.response;

import java.time.Instant;

// 어드민 대화 로그 조회 응답. filtered=true 면 차단된 질문이며 answer 에 거절 메시지가 담긴다.
public record AdminChatLogResponse(
        Long chatLogId,
        Long userId,
        String userName,
        String userNickname,
        String question,
        String answer,
        boolean filtered,
        Instant createdAt
) {}
