package com.kidmily.algoga_server.example.presentation.api;

import com.kidmily.algoga_server.example.application.command.CreateExampleCommand;
import com.kidmily.algoga_server.example.application.usecase.ExampleCommandUseCase;
import com.kidmily.algoga_server.example.exception.ExampleErrorCode;
import com.kidmily.algoga_server.example.presentation.api.request.CreateExampleRequest;
import com.kidmily.algoga_server.example.presentation.api.response.ExampleResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
@Tag(name = "Example", description = "예시 도메인 API")
public class ExampleController {

    private final ExampleCommandUseCase exampleCommandUseCase;

    @PostMapping
    @Operation(summary = "예시 데이터 생성", description = "새로운 예시 데이터를 생성합니다.")

    // 🌟 중첩(Repeatable) 활용:
    // 1) @Valid 껍데기 검증 실패 시 (정적 에러 - 예: 공백 입력)
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    // 2) UseCase 비즈니스 로직 검증 실패 시 (동적 에러 - 예: 정책 위반 이름 입력)
    @ApiErrorCodeExample(domain = ExampleErrorCode.class, value = {"INVALID_EXAMPLE_NAME"})
    public ResponseEntity<ApiResponse<ExampleResponse>> createExample(
            @Valid @RequestBody CreateExampleRequest request
    ) {
        CreateExampleCommand command = new CreateExampleCommand(request.name());
        Long createdId = exampleCommandUseCase.handle(command);
        ExampleResponse responseData = new ExampleResponse(createdId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("EXAMPLE_CREATED", "예시 생성에 성공했습니다.", responseData));
    }

    @GetMapping("/{exampleId}")
    @Operation(summary = "예시 데이터 단건 조회", description = "ID로 예시 데이터를 조회합니다.")
    @ApiErrorCodeExample(domain = ExampleErrorCode.class, value = {"EXAMPLE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<ExampleResponse>> getExample(
            @PathVariable Long exampleId
    ) {
        ExampleResponse responseData = new ExampleResponse(exampleId);

        return ResponseEntity.ok(ApiResponse.success(
                "EXAMPLE_FOUND",
                "조회에 성공했습니다.",
                responseData
        ));
    }
}