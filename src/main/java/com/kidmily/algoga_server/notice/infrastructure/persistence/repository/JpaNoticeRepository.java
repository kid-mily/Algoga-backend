package com.kidmily.algoga_server.notice.infrastructure.persistence.repository;

import com.kidmily.algoga_server.notice.infrastructure.persistence.entity.NoticeEntity;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findTop3ByOrderByCreatedAtDesc();
    
    // 🌟 List 대신 Page 로 반환 타입 변경
    Page<NoticeEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<NoticeEntity> findByTypeOrderByCreatedAtDesc(NoticeTagType type, Pageable pageable);
}