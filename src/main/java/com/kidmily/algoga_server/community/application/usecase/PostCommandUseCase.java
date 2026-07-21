package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.application.command.AdminDeletePostCommand;
import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;

// 외부 계층(컨트롤러)가 바라보는 인터페이스
public interface PostCommandUseCase {
    Long handle(CreatePostCommand command);
    Long handle(UpdatePostCommand command);
    void handle(DeletePostCommand command);
    void handle(AdminDeletePostCommand command);
}
