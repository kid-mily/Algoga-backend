package com.kidmily.algoga_server.notice.application.command;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;

public record CreateNoticeCommand(
        String title,
        String content,
        NoticeTagType noticeTagType
) {
}
