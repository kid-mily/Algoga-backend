package com.kidmily.algoga_server.chatbot.application.dto;

import java.time.Instant;

// 어드민 대화 로그 조회 애플리케이션 계층 DTO (작성자 이름/닉네임 포함)
public record AdminChatLogDto(
        Long chatLogId,
        Long userId,
        String userName,
        String userNickname,
        String question,
        String answer,
        boolean filtered,
        Instant createdAt
) {}
