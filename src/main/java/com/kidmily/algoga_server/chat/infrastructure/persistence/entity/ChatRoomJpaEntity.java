package com.kidmily.algoga_server.chat.infrastructure.persistence.entity;

import com.kidmily.algoga_server.chat.domain.model.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@SQLRestriction("is_deleted = false")
@Entity
@Table(
        name = "chat_rooms",
        indexes = {
                @Index(name = "idx_chat_rooms_deleted",
                        columnList = "is_deleted, deleted_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "room_name")
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")                    // ▼ 추가
    private LocalDateTime deletedAt;

    public void delete() {

        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }




}