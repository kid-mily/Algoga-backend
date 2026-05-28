package com.kidmily.algoga_server.notification.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    private Long userId;
    private Boolean learningEnabled;
    private Boolean qnaEnabled;
    private Boolean communityEnabled;
    private Boolean noticeEnabled;
    private Boolean inquiryEnabled;
    private Boolean friendEnabled;

    private NotificationSetting(Long userId) {
        this.userId = userId;
        this.learningEnabled = true;
        this.qnaEnabled = true;
        this.communityEnabled = true;
        this.noticeEnabled = true;
        this.inquiryEnabled = true;
        this.friendEnabled = true;
    }

    private NotificationSetting(Long userId, Boolean learningEnabled, Boolean qnaEnabled,
                                Boolean communityEnabled, Boolean noticeEnabled,
                                Boolean inquiryEnabled, Boolean friendEnabled) {
        this.userId = userId;
        this.learningEnabled = learningEnabled;
        this.qnaEnabled = qnaEnabled;
        this.communityEnabled = communityEnabled;
        this.noticeEnabled = noticeEnabled;
        this.inquiryEnabled = inquiryEnabled;
        this.friendEnabled = friendEnabled;
    }

    public static NotificationSetting createDefault(Long userId) {
        return new NotificationSetting(userId);
    }

    public static NotificationSetting reconstitute(Long userId, Boolean learningEnabled, Boolean qnaEnabled,
                                                   Boolean communityEnabled, Boolean noticeEnabled,
                                                   Boolean inquiryEnabled, Boolean friendEnabled) {
        return new NotificationSetting(userId, learningEnabled, qnaEnabled,
                communityEnabled, noticeEnabled, inquiryEnabled, friendEnabled);
    }

    public boolean isEnabledFor(NotificationCategory category) {
        return switch (category) {
            case LEARNING -> learningEnabled;
            case QNA -> qnaEnabled;
            case COMMUNITY -> communityEnabled;
            case NOTICE -> noticeEnabled;
            case INQUIRY -> inquiryEnabled;
            case FRIEND -> friendEnabled;
            case MANDATORY -> true;  // 필수 알림은 항상 활성
        };
    }

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