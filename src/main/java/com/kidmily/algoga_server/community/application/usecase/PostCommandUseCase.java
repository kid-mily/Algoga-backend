package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;
import com.kidmily.algoga_server.example.application.command.CreateExampleCommand;

// 외부 계층(컨트롤러)가 바라보는 인터페이스
public interface PostCommandUseCase {
    Long handle(CreatePostCommand command);
    Long handle(UpdatePostCommand command);
    void handle(DeletePostCommand command);
}
