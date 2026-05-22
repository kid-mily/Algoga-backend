package com.kidmily.algoga_server.notice.application.service;

import com.kidmily.algoga_server.notice.application.usecase.NoticeQueryUseCase;
import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeRepository noticeRepository;
    private static final int PAGE_SIZE = 10;

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(SEOUL_ZONE);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(SEOUL_ZONE);
    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(SEOUL_ZONE);

    @Override
    public List<NoticeMainResponse> getNoticeMain() {
        return noticeRepository.findTop3Notices().stream()
                .map(notice -> new NoticeMainResponse(
                        notice.getNoticeId(),
                        notice.getType(),
                        notice.getTitle(),
                        DATE_FORMATTER.format(notice.getCreatedAt()),
                        TIME_FORMATTER.format(notice.getCreatedAt())
                )).collect(Collectors.toList());
    }

    @Override
    // 🔥 반환 타입 수정
    public List<NoticeListResponse> getNotices(String tag, Integer index) {
        List<Notice> notices;
        int pageIndex = Math.max(0, index - 1);

        try {
            if ("ALL".equalsIgnoreCase(tag)) {
                notices = noticeRepository.findAll(pageIndex, PAGE_SIZE);
            } else {
                NoticeTagType tagType = NoticeTagType.valueOf(tag.toUpperCase());
                notices = noticeRepository.findByType(tagType, pageIndex, PAGE_SIZE);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(NoticeErrorCode.INVALID_TAG_OR_INDEX.getMessage());
        }

        // 🔥 NoticeListResponse 에 맞춰 매핑 (날짜 포맷 적용)
        return notices.stream()
                .map(notice -> new NoticeListResponse(
                        notice.getNoticeId(),
                        notice.getType(),
                        notice.getTitle(),
                        DATE_FORMATTER.format(notice.getCreatedAt()) // yyyy-MM-dd
                ))
                .collect(Collectors.toList());
    }

    @Override
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage())
        );
        return mapToNoticeResponse(notice);
    }

    private NoticeResponse mapToNoticeResponse(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getManagerId(),
                notice.getType(),
                notice.getTitle(),
                notice.getContent(),
                FULL_FORMATTER.format(notice.getCreatedAt())
        );
    }
}