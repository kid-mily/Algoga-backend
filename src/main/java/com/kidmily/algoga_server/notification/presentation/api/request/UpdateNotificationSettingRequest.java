package com.kidmily.algoga_server.notification.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 수신 설정 변경 요청 (변경할 항목만 전송, 나머지는 생략 가능)")
public record UpdateNotificationSettingRequest(

        @Schema(description = "학습 알림 수신 여부", example = "true")
        Boolean learningEnabled,

        @Schema(description = "Q&A 알림 수신 여부", example = "true")
        Boolean qnaEnabled,

        @Schema(description = "커뮤니티 알림 수신 여부", example = "true")
        Boolean communityEnabled,

        @Schema(description = "공지사항 알림 수신 여부", example = "true")
        Boolean noticeEnabled,

        @Schema(description = "문의 알림 수신 여부", example = "true")
        Boolean inquiryEnabled,

        @Schema(description = "친구 알림 수신 여부", example = "true")
        Boolean friendEnabled
) {}