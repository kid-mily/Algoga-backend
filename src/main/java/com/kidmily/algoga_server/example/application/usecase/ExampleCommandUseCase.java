package com.kidmily.algoga_server.example.application.usecase;

import com.kidmily.algoga_server.example.application.command.CreateExampleCommand;

// 외부 계층(Controller)이 바라보는 인터페이스
public interface ExampleCommandUseCase {
    Long handle(CreateExampleCommand command);
}