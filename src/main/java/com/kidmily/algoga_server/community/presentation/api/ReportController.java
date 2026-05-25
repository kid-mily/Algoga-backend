package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.command.CreateReportCommand;
import com.kidmily.algoga_server.community.application.usecase.ReportCommandUseCase;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.presentation.api.request.CreateReportRequest;
import com.kidmily.algoga_server.community.presentation.api.response.CreateReportResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class ReportController {

    private final ReportCommandUseCase reportCommandUseCase;

    @PostMapping
    @Operation(summary = "게시글/댓글 신고", description = "부적절한 게시글 또는 댓글을 신고합니다. 동일 대상 중복 신고 불가.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신고가 접수되었습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "REPORT_UNAUTHORIZED",
            "POST_NOT_FOUND",
            "COMMENT_NOT_FOUND",
            "REPORT_DUPLICATED"
    })
    public ResponseEntity<ApiResponse<CreateReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request
    ) {
        Long currentUserId = 1L;

        CreateReportCommand command = new CreateReportCommand(
                currentUserId,
                request.targetId(),
                request.targetType(),
                request.reasonType(),
                request.detail()
        );

        Long reportId = reportCommandUseCase.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("REPORT_CREATED", "신고가 접수되었습니다.",
                        new CreateReportResponse(reportId)));
    }
}