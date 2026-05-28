package com.kidmily.algoga_server.notice.application.service;

import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;
import com.kidmily.algoga_server.notice.application.usecase.NoticeCommandUseCase;
import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.exception.NoticeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public Long registerNotice(CreateNoticeCommand request) {
        Instant now = Instant.now();

        // 🌟 하드코딩 되어있던 currentManagerId 대신 request.managerId() 사용
        Notice newNotice = Notice.create(request.managerId(), request.noticeTagType(), request.title(), request.content(), now);
        Long savedId = noticeRepository.save(newNotice).getNoticeId();

        log.info("[Notice Created] noticeId: {}, managerId: {}, type: {}", savedId, request.managerId(), request.noticeTagType());
        return savedId;
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).orElseThrow(() ->
                new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        );

        noticeRepository.deleteById(noticeId);
        log.info("[Notice Deleted] noticeId: {}", noticeId);
    }

    @Override
    @Transactional
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