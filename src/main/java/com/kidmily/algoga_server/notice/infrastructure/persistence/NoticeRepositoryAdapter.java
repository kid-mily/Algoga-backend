package com.kidmily.algoga_server.notice.infrastructure.persistence;

import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.infrastructure.mapper.NoticeMapper;
import com.kidmily.algoga_server.notice.infrastructure.persistence.entity.NoticeEntity;
import com.kidmily.algoga_server.notice.infrastructure.persistence.repository.JpaNoticeRepository;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoticeRepositoryAdapter implements NoticeRepository {

    private final JpaNoticeRepository jpaNoticeRepository;
    private final NoticeMapper noticeMapper;

    @Override
    public Notice save(Notice notice) {
        NoticeEntity entity = noticeMapper.toEntity(notice);
        return noticeMapper.toDomain(jpaNoticeRepository.save(entity));
    }

    @Override
    public Optional<Notice> findById(Long noticeId) {
        return jpaNoticeRepository.findById(noticeId).map(noticeMapper::toDomain);
    }

    @Override
    public void deleteById(Long noticeId) {
        jpaNoticeRepository.deleteById(noticeId);
    }

    @Override
    public List<Notice> findTop3Notices() {
        return jpaNoticeRepository.findTop3ByOrderByCreatedAtDesc().stream()
                .map(noticeMapper::toDomain)
                .collect(Collectors.toList());
    }

    // 🌟 Page 객체 매핑
    @Override
    public Page<Notice> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaNoticeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(noticeMapper::toDomain);
    }

    // 🌟 Page 객체 매핑
    @Override
    public Page<Notice> findByType(NoticeTagType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaNoticeRepository.findByTypeOrderByCreatedAtDesc(type, pageable)
                .map(noticeMapper::toDomain);
    }
}