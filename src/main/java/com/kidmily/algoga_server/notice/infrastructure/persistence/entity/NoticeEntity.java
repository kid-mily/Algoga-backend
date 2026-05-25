package com.kidmily.algoga_server.notice.infrastructure.persistence.entity;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false)
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeTagType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public NoticeEntity(Long noticeId, Long managerId, NoticeTagType type, String title, String content, Instant createdAt) {
        this.noticeId = noticeId;
        this.managerId = managerId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}