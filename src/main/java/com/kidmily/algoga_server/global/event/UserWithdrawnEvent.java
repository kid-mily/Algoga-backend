package com.kidmily.algoga_server.global.event;

// 다른 도메인(쿠폰, 알림 등)에게 "이 유저 탈퇴했어!"라고 알려줄 데이터 명세서입니다.
public record UserWithdrawnEvent(
        Long userId,    // 탈퇴한 유저의 식별자
        String email    // 필요 시 이메일 (선택)
) {
}