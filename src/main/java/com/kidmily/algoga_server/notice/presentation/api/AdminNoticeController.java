package com.kidmily.algoga_server.notice.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;
import com.kidmily.algoga_server.notice.application.usecase.NoticeCommandUseCase;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.presentation.api.request.CreateNoticeRequest;
import com.kidmily.algoga_server.notice.presentation.api.request.UpdateNoticeRequest;
import com.kidmily.algoga_server.notice.presentation.api.response.CreateNoticeResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.UpdateNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notices") // 🌟 어드민 전용 경로
@RequiredArgsConstructor
@Tag(name = "Admin Notice", description = "어드민 전용 공지사항 관리 API")
public class AdminNoticeController {

    private final NoticeCommandUseCase noticeCommandUseCase;

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')") // 🌟 권한 체크
    @PostMapping("/register")
    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다. (권한: CS_MANAGER 이상)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {
            "NOTICE_TYPE_REQUIRED", "TITLE_REQUIRED", "TITLE_LENGTH_EXCEEDED",
            "CONTENT_REQUIRED", "CONTENT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<CreateNoticeResponse>> registerNotice(
            @Valid @RequestBody CreateNoticeRequest request,
            @CurrentManager Long managerId // 🌟 현재 로그인한 어드민 ID 주입
    ) {
        // managerId를 Command에 담아 Service로 전달합니다.
        Long noticeId = noticeCommandUseCase.registerNotice(new CreateNoticeCommand(
                request.title(),
                request.content(),
                request.type(),
                managerId
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("NOTICE_CREATED", "공지사항 등록에 성공했습니다.", new CreateNoticeResponse(noticeId)));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @PutMapping("/modify/{noticeId}")
    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다. (권한: CS_MANAGER 이상)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {
            "NOTICE_NOT_FOUND", "NOTICE_TYPE_REQUIRED", "TITLE_REQUIRED",
            "TITLE_LENGTH_EXCEEDED", "CONTENT_REQUIRED", "CONTENT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<UpdateNoticeResponse>> modifyNotice(
            @Parameter(description = "수정할 공지사항 ID", example = "1") @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest request,
            @CurrentManager Long managerId // (수정 로직에 필요할 경우 사용)
    ) {
        noticeCommandUseCase.modifyNotice(noticeId, new UpdateNoticeCommand(
                request.title(),
                request.content(),
                request.type()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_MODIFIED", "공지사항 수정에 성공했습니다.", new UpdateNoticeResponse(noticeId)
        ));
    }

    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다. (권한: CS_MANAGER 이상)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"NOTICE_NOT_FOUND"})
    public ResponseEntity<Void> deleteNotice(
            @Parameter(description = "삭제할 공지사항 ID", example = "1") @PathVariable Long noticeId,
            @CurrentManager Long managerId
    ) {
        noticeCommandUseCase.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}