package com.kidmily.algoga_server.notice.application.service;

import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;
import com.kidmily.algoga_server.notice.application.port.UserPort;
import com.kidmily.algoga_server.notice.application.usecase.NoticeCommandUseCase;
import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.exception.NoticeException;
import com.kidmily.algoga_server.notice.settings.cache.NoticeCacheType;
import com.kidmily.algoga_server.notification.domain.event.NoticeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserPort userPort;

    @Override
    @Transactional
    @CacheEvict(cacheNames = {NoticeCacheType.Const.NOTICE_MAIN, NoticeCacheType.Const.NOTICE_LIST}, allEntries = true)
    public Long registerNotice(CreateNoticeCommand request) {
        Instant now = Instant.now();

        // 🌟 하드코딩 되어있던 currentManagerId 대신 request.managerId() 사용
        Notice newNotice = Notice.create(request.managerId(), request.noticeTagType(), request.title(), request.content(), now);
        Long savedId = noticeRepository.save(newNotice).getNoticeId();

        List<Long> userIds = userPort.findAllActiveUserIds();
        userIds.forEach(userId ->
                eventPublisher.publishEvent(new NoticeCreatedEvent(userId, savedId, request.title(), request.content()))
        );

        log.info("[Notice Created] noticeId: {}, managerId: {}, type: {}", savedId, request.managerId(), request.noticeTagType());
        return savedId;
    }

    @Override
    @Transactional
    @Caching(evict = {
            // 🌟 하드코딩 제거
            @CacheEvict(cacheNames = NoticeCacheType.Const.NOTICE_DETAIL, key = "#noticeId"),
            @CacheEvict(cacheNames = {NoticeCacheType.Const.NOTICE_MAIN, NoticeCacheType.Const.NOTICE_LIST}, allEntries = true)
    })
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).orElseThrow(() ->
                new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        );

        noticeRepository.deleteById(noticeId);
        log.info("[Notice Deleted] noticeId: {}", noticeId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            // 🌟 하드코딩 제거
            @CacheEvict(cacheNames = NoticeCacheType.Const.NOTICE_DETAIL, key = "#noticeId"),
            @CacheEvict(cacheNames = {NoticeCacheType.Const.NOTICE_MAIN, NoticeCacheType.Const.NOTICE_LIST}, allEntries = true)
    })
    public void modifyNotice(Long noticeId, UpdateNoticeCommand command) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        );

        // 🔥 command.type() -> command.noticeTagType() 으로 수정
        Notice updatedNotice = notice.update(
                command.noticeTagType(),
                command.title(),
                command.content()
        );
        noticeRepository.save(updatedNotice);

        // 🔥 command.type() -> command.noticeTagType() 으로 수정
        log.info("[Notice Modified] noticeId: {}, newType: {}", noticeId, command.noticeTagType());
    }
}