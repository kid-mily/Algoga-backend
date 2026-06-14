package com.kidmily.algoga_server.report.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.report.application.command.CreateReportCommand;
import com.kidmily.algoga_server.report.application.command.UpdateReportStatusCommand;
import com.kidmily.algoga_server.report.application.usecase.ReportCommandUseCase;
import com.kidmily.algoga_server.report.exception.ReportErrorCode;
import com.kidmily.algoga_server.report.presentation.api.request.CreateReportRequest;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportDetailResponse;
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
import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.report.application.usecase.ReportQueryUseCase;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.presentation.api.response.AdminReportListResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Tag(name = "Report", description = "신고 도메인 API")
public class ReportController {

    private final ReportCommandUseCase reportCommandUseCase;
    private final ReportQueryUseCase reportQueryUseCase;

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

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin")
    @Operation(summary = "신고 내역 목록 조회 (관리자용)", description = "CS 관리자가 전체 신고 내역을 페이지 번호 기반으로 조회합니다. 처리 상태/신고 유형 필터링, 신고자·피신고자 닉네임 검색이 가능합니다.")
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports(
            @Parameter(description = "처리 상태 필터 (RECEIVED/REJECTED/COMPLETED)")
            @RequestParam(required = false) ReportStatus status,

            @Parameter(description = "신고 유형 필터 (POST/COMMENT)")
            @RequestParam(required = false) TargetType targetType,

            @Parameter(description = "신고자/피신고자 닉네임 검색")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "특정 피신고자 ID로 필터링 (블랙리스트 후보 상세 등에서 사용)")
            @RequestParam(required = false) Long reportedUserId,

            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") Integer index,

            @CurrentManager Long managerId
    ) {
        AdminReportListResponse responseData = reportQueryUseCase.getReportsByPage(status, targetType, keyword, reportedUserId, index);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_REPORTS_FOUND", "신고 내역 조회에 성공했습니다.", responseData));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin/{reportId}")
    @Operation(summary = "신고 상세 조회 (관리자용)", description = "CS 관리자가 신고 상세 정보와 신고 대상(게시글/댓글) 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = ReportErrorCode.class, value = {"REPORT_NOT_FOUND"})
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReportDetail(
            @Parameter(description = "신고 ID", example = "1")
            @PathVariable Long reportId,
            @CurrentManager Long managerId
    ) {
        AdminReportDetailResponse responseData = reportQueryUseCase.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_REPORT_FOUND", "신고 상세 조회에 성공했습니다.", responseData));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PatchMapping("/admin/{reportId}/reject")
    @Operation(summary = "신고 반려 (관리자용)", description = "CS 관리자가 신고를 반려 처리합니다.")
    public ResponseEntity<Void> rejectReport(
            @PathVariable Long reportId,
            @CurrentManager Long managerId
    ) {
        reportCommandUseCase.updateStatus(new UpdateReportStatusCommand(reportId, ReportStatus.REJECTED));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PatchMapping("/admin/{reportId}/complete")
    @Operation(summary = "신고 처리완료 (관리자용)", description = "CS 관리자가 신고를 처리완료 처리합니다.")
    public ResponseEntity<Void> completeReport(
            @PathVariable Long reportId,
            @CurrentManager Long managerId
    ) {
        reportCommandUseCase.updateStatus(new UpdateReportStatusCommand(reportId, ReportStatus.COMPLETED));
        return ResponseEntity.noContent().build();
    }
}