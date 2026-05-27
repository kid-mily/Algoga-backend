package com.kidmily.algoga_server.notice.domain.repository;

import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository {
    Notice save(Notice notice);
    Optional<Notice> findById(Long noticeId);
    void deleteById(Long noticeId);

    List<Notice> findTop3Notices();
    List<Notice> findAll(int page, int size);
    List<Notice> findByType(NoticeTagType type, int page, int size);
}