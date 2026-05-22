package com.kidmily.algoga_server.notice.application.usecase;

import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import java.util.List;

public interface NoticeQueryUseCase {
    List<NoticeMainResponse> getNoticeMain();

    // 🔥 반환 타입을 NoticeListResponse로 수정
    List<NoticeListResponse> getNotices(String tag, Integer index);

    NoticeResponse getNotice(Long noticeId);
}