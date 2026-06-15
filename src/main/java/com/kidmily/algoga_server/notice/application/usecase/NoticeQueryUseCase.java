package com.kidmily.algoga_server.notice.application.usecase;

import com.kidmily.algoga_server.global.common.api.response.PageResponse; // 🌟 추가
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import java.util.List;

public interface NoticeQueryUseCase {
    List<NoticeMainResponse> getNoticeMain();
    
    // 🌟 반환 타입 변경
    PageResponse<NoticeListResponse> getNotices(String tag, Integer index);
    
    NoticeResponse getNotice(Long noticeId);
    List<NoticeTagType> getAllNoticeTags();
}