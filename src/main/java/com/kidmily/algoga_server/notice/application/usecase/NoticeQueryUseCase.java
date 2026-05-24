package com.kidmily.algoga_server.notice.application.usecase;

import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import java.util.List;

public interface NoticeQueryUseCase {
    List<NoticeMainResponse> getNoticeMain();
    List<NoticeListResponse> getNotices(String tag, Integer index);
    NoticeResponse getNotice(Long noticeId);

    // 🔥 프론트엔드 UI 구성을 위한 태그 목록 조회 메서드 추가
    List<NoticeTagType> getAllNoticeTags();
}