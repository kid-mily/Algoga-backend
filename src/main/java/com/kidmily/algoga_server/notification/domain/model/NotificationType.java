package com.kidmily.algoga_server.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 학습
    COURSE_REGISTERED("강의 수강 등록", NotificationCategory.LEARNING),
    COURSE_COMPLETED("강의 수강 완료", NotificationCategory.LEARNING),
    DDAY_REMINDER("D-day 알림", NotificationCategory.LEARNING),

    // Q&A
    QNA_ANSWERED("Q&A 답변 등록", NotificationCategory.QNA),

    // 커뮤니티
    POST_COMMENTED("게시글 댓글", NotificationCategory.COMMUNITY), // 완료
    COMMENT_REPLIED("댓글 대댓글", NotificationCategory.COMMUNITY), //

    // 결제/예약 (필수 알림 - 설정 무관)
    PAYMENT_COMPLETED("결제 완료", NotificationCategory.MANDATORY), // 완료
    RESERVATION_CONFIRMED("예약 확정", NotificationCategory.MANDATORY),
    REFUND_APPROVED("환불 승인", NotificationCategory.MANDATORY),
    REFUND_REJECTED("환불 거절", NotificationCategory.MANDATORY),

    // 공지사항
    NOTICE_CREATED("공지사항 등록", NotificationCategory.NOTICE),

    // 문의
    INQUIRY_ANSWERED("문의 답변", NotificationCategory.INQUIRY),

    // 친구
    FRIEND_REQUESTED("친구 요청", NotificationCategory.FRIEND),
    FRIEND_ACCEPTED("친구 수락", NotificationCategory.FRIEND),

    // 시스템
    SYSTEM("시스템 알림", NotificationCategory.MANDATORY);

    private final String description;
    private final NotificationCategory category;
}