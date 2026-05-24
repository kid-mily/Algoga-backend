package com.kidmily.algoga_server.notice.application.usecase;

import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;

public interface NoticeCommandUseCase {
    Long registerNotice(CreateNoticeCommand request);
    void deleteNotice(Long noticeId);
    void modifyNotice(Long noticeId, UpdateNoticeCommand command);
}