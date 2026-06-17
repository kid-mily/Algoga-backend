package com.kidmily.algoga_server.blacklist.infrastructure.persistence.entity;

import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime unblacklistedAt;

    @Builder
    public BlacklistJpaEntity(Long id, Long userId, String reason, BlacklistStatus status, LocalDateTime createdAt, LocalDateTime unblacklistedAt) {
        this.id = id;
        this.userId = userId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.unblacklistedAt = unblacklistedAt;
    }
}