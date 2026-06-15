package com.kidmily.algoga_server.notice.application.service;
import com.kidmily.algoga_server.global.common.api.response.PageResponse; // 🌟 추가
import com.kidmily.algoga_server.notice.application.usecase.NoticeQueryUseCase;
import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.exception.NoticeException;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import org.springframework.data.domain.Page; // 🌟 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class    NoticeQueryService implements NoticeQueryUseCase {

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
    public PageResponse<NoticeListResponse> getNotices(String tag, Integer index) {
        Page<Notice> noticePage;
        int pageIndex = Math.max(0, index - 1);

        try {
            if ("ALL".equalsIgnoreCase(tag)) {
                noticePage = noticeRepository.findAll(pageIndex, PAGE_SIZE);
            } else {
                NoticeTagType tagType = NoticeTagType.valueOf(tag.toUpperCase());
                noticePage = noticeRepository.findByType(tagType, pageIndex, PAGE_SIZE);
            }
        } catch (IllegalArgumentException e) {
            throw new NoticeException(NoticeErrorCode.INVALID_TAG_OR_INDEX);
        }

        // 도메인 Page 객체를 응답 DTO Page 객체로 변환
        Page<NoticeListResponse> responsePage = noticePage.map(notice -> new NoticeListResponse(
                notice.getNoticeId(),
                notice.getType(),
                notice.getTitle(),
                DATE_FORMATTER.format(notice.getCreatedAt())
        ));

        // 글로벌 공통 응답으로 Wrapping 하여 반환
        return PageResponse.from(responsePage);
    }

    @Override
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                // 🔥 NoticeException 으로 변경
                new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        );
        return mapToNoticeResponse(notice);
    }

    @Override
    public List<NoticeTagType> getAllNoticeTags() {
        // Enum에 정의된 모든 값을 리스트 형태로 반환합니다.
        return java.util.Arrays.asList(NoticeTagType.values());
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