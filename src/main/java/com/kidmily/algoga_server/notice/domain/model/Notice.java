package com.kidmily.algoga_server.notice.domain.model;

import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.exception.NoticeException;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Notice {
    private final Long noticeId;
    private final Long managerId;
    private final NoticeTagType type;
    private final String title;
    private final String content;
    private final Instant createdAt;

    @Builder
    public Notice(Long noticeId, Long managerId, NoticeTagType type, String title, String content, Instant createdAt) {
        this.noticeId = noticeId;
        this.managerId = managerId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Notice create(Long managerId, NoticeTagType type, String title, String content, Instant createdAt) {
        validateType(type);
        validateTitle(title);
        validateContent(content);

        return Notice.builder()
                .managerId(managerId)
                .type(type)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

    public Notice update(NoticeTagType newType, String newTitle, String newContent) {
        validateType(newType);
        validateTitle(newTitle);
        validateContent(newContent);

        return Notice.builder()
                .noticeId(this.noticeId)
                .managerId(this.managerId)
                .type(newType)
                .title(newTitle)
                .content(newContent)
                .createdAt(this.createdAt)
                .build();
    }

    // 🔥 NoticeException 으로 변경
    private static void validateType(NoticeTagType type) {
        if (type == null) {
            throw new NoticeException(NoticeErrorCode.NOTICE_TYPE_REQUIRED);
        }
    }

    // 🔥 NoticeException 으로 변경
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new NoticeException(NoticeErrorCode.TITLE_REQUIRED);
        }
        if (title.length() < 2 || title.length() > 100) {
            throw new NoticeException(NoticeErrorCode.TITLE_LENGTH_EXCEEDED);
        }
    }

    // 🔥 NoticeException 으로 변경
    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new NoticeException(NoticeErrorCode.CONTENT_REQUIRED);
        }
        // DB 스키마가 tinytext (최대 255바이트)일 경우 제한 예시
        if (content.length() > 255) {
            throw new NoticeException(NoticeErrorCode.CONTENT_LENGTH_EXCEEDED);
        }
    }
}