package com.kidmily.algoga_server.notice.application.service;

import com.kidmily.algoga_server.example.application.command.CreateExampleCommand;
import com.kidmily.algoga_server.example.application.usecase.ExampleCommandUseCase;
import com.kidmily.algoga_server.example.domain.model.Example;
import com.kidmily.algoga_server.example.domain.repository.ExampleRepository;
import com.kidmily.algoga_server.example.exception.ExampleErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExampleCommandService implements ExampleCommandUseCase {

    private final ExampleRepository exampleRepository;

    @Override
    public Long handle(CreateExampleCommand command) {
        // 비즈니스 규칙 검증 및 예외 처리
        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessException(ExampleErrorCode.INVALID_EXAMPLE_NAME);
        }

        // 도메인 엔티티 생성
        Example newExample = Example.create(command.name());

        // 상태 저장 (영속성 처리)
        Example savedExample = exampleRepository.save(newExample);

        return savedExample.getId();
    }
}