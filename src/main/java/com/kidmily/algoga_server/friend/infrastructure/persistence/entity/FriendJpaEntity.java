package com.kidmily.algoga_server.friend.infrastructure.persistence.entity;

import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
// 아래처럼 indexes 옵션을 추가해주세요!
@Table(name = "friend_relations", indexes = {
        @Index(name = "idx_requester_status", columnList = "requesterId, status"),
        @Index(name = "idx_receiver_status", columnList = "receiverId, status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FriendJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationStatus status;

    @Column(nullable = false)
    private boolean favorite;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}