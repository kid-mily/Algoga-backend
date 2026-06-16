package com.kidmily.algoga_server.blacklist.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Blacklist {
    private final Long id;
    private final Long userId;
    private final String reason;
    private BlacklistStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime unblacklistedAt;

    @Builder
    public Blacklist(Long id, Long userId, String reason, BlacklistStatus status, LocalDateTime createdAt, LocalDateTime unblacklistedAt) {
        this.id = id;
        this.userId = userId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.unblacklistedAt = unblacklistedAt;
    }

    public static Blacklist create(Long userId, String reason) {
        return Blacklist.builder()
                .userId(userId)
                .reason(reason)
                .status(BlacklistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void deregister() {
        this.status = BlacklistStatus.INACTIVE;
        this.unblacklistedAt = LocalDateTime.now();
    }
}