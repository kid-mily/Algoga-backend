package com.kidmily.algoga_server.friend.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class FriendRelation {
    private Long id;
    private Long requesterId;
    private Long receiverId;
    private RelationStatus status;
    private boolean favorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void accept() {
        this.status = RelationStatus.ACCEPTED;
    }

    public void updateStatus(RelationStatus status) {
        this.status = status;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }
}