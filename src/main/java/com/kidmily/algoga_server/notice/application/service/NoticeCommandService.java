package com.kidmily.algoga_server.notice.application.service;

import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;
import com.kidmily.algoga_server.notice.application.usecase.NoticeCommandUseCase;
import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.domain.repository.NoticeRepository;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public Long registerNotice(CreateNoticeCommand request) {
        Long currentManagerId = 1L; // SecurityContext 등에서 추출 예정
        Instant now = Instant.now();

        Notice newNotice = Notice.create(currentManagerId, request.noticeTagType(), request.title(), request.content(), now);
        return noticeRepository.save(newNotice).getNoticeId();
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage())
        );
        noticeRepository.deleteById(noticeId);
    }

    @Override
    @Transactional
    public void modifyNotice(Long noticeId, UpdateNoticeCommand command) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage())
        );

        Notice updatedNotice = notice.update(
                command.noticeTagType(),
                command.title(),
                command.content()
        );
        noticeRepository.save(updatedNotice);
    }
}