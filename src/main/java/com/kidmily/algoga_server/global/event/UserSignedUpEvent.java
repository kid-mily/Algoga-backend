package com.kidmily.algoga_server.global.event;

public record UserSignedUpEvent(
        Long userId,
        Long referrerUserId,
        String referralCode
) {
    public UserSignedUpEvent(Long userId) {
        this(userId, null, null);
    }
}
