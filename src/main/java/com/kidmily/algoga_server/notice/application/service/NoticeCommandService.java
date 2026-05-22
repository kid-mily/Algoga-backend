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
        // 1. 기존 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                new IllegalArgumentException(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage())
        );

        // 2. command.noticeTagType()을 통해 태그 변경 요청을 전달하여 새 도메인 객체 생성
        Notice updatedNotice = notice.update(
                command.noticeTagType(), // 🔥 여기서 수정할 태그 타입이 전달됩니다.
                command.title(),
                command.content()
        );

        // 3. 어댑터를 거쳐 영속성 엔티티로 변환 후 갱신(Update)
        noticeRepository.save(updatedNotice);
    }
}