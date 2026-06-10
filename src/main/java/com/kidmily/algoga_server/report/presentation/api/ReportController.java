package com.kidmily.algoga_server.report.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.report.application.command.CreateReportCommand;
import com.kidmily.algoga_server.report.application.usecase.ReportCommandUseCase;
import com.kidmily.algoga_server.report.exception.ReportErrorCode;
import com.kidmily.algoga_server.report.presentation.api.request.CreateReportRequest;
import com.kidmily.algoga_server.report.presentation.api.response.CreateReportResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Tag(name = "Report", description = "신고 도메인 API")
public class ReportController {

    private final ReportCommandUseCase reportCommandUseCase;

    @PostMapping
    @Operation(summary = "게시글/댓글 신고", description = "부적절한 게시글 또는 댓글을 신고합니다. 동일 대상 중복 신고 불가.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신고가 접수되었습니다.")
    @ApiErrorCodeExample(domain = ReportErrorCode.class, value = {
            "REPORT_UNAUTHORIZED",
            "REPORT_TARGET_NOT_FOUND",
            "REPORT_SELF_NOT_ALLOWED",
            "REPORT_DUPLICATED"
    })
    public ResponseEntity<ApiResponse<CreateReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

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