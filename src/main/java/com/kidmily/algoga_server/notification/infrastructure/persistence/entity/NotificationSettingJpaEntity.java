package com.kidmily.algoga_server.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationSettingJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "learning_enabled")
    @Builder.Default
    private Boolean learningEnabled = true;

    @Column(name = "qna_enabled")
    @Builder.Default
    private Boolean qnaEnabled = true;

    @Column(name = "community_enabled")
    @Builder.Default
    private Boolean communityEnabled = true;

    @Column(name = "notice_enabled")
    @Builder.Default
    private Boolean noticeEnabled = true;

    @Column(name = "inquiry_enabled")
    @Builder.Default
    private Boolean inquiryEnabled = true;

    @Column(name = "friend_enabled")
    @Builder.Default
    private Boolean friendEnabled = true;

    public void update(Boolean learningEnabled, Boolean qnaEnabled, Boolean communityEnabled,
                       Boolean noticeEnabled, Boolean inquiryEnabled, Boolean friendEnabled) {
        if (learningEnabled != null) this.learningEnabled = learningEnabled;
        if (qnaEnabled != null) this.qnaEnabled = qnaEnabled;
        if (communityEnabled != null) this.communityEnabled = communityEnabled;
        if (noticeEnabled != null) this.noticeEnabled = noticeEnabled;
        if (inquiryEnabled != null) this.inquiryEnabled = inquiryEnabled;
        if (friendEnabled != null) this.friendEnabled = friendEnabled;
    }
}