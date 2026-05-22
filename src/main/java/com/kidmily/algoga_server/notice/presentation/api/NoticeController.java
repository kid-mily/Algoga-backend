package com.kidmily.algoga_server.notice.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;

import com.kidmily.algoga_server.notice.application.command.CreateNoticeCommand;
import com.kidmily.algoga_server.notice.application.command.UpdateNoticeCommand;
import com.kidmily.algoga_server.notice.application.usecase.NoticeCommandUseCase;
import com.kidmily.algoga_server.notice.application.usecase.NoticeQueryUseCase;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.presentation.api.request.CreateNoticeRequest;
import com.kidmily.algoga_server.notice.presentation.api.request.UpdateNoticeRequest;
import com.kidmily.algoga_server.notice.presentation.api.response.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notice")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지사항 도메인 API")
public class NoticeController {

    private final NoticeCommandUseCase noticeCommandUseCase;
    private final NoticeQueryUseCase noticeQueryUseCase;

    @GetMapping("/main")
    @Operation(summary = "공지사항 메인 조회", description = "메인 페이지에 노출되는 최신 공지사항 3개를 조회합니다.")
    public ResponseEntity<ApiResponse<List<NoticeMainResponse>>> getNoticeMain() {
        List<NoticeMainResponse> responseData = noticeQueryUseCase.getNoticeMain();

        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_MAIN_FOUND",
                "메인 공지사항 조회에 성공했습니다.",
                responseData
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다. (권한: CS_MANAGER)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {
            "NOTICE_TYPE_REQUIRED", "TITLE_REQUIRED", "TITLE_LENGTH_EXCEEDED",
            "CONTENT_REQUIRED", "CONTENT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<CreateNoticeResponse>> registerNotice(
            @Valid @RequestBody CreateNoticeRequest request
    ) {
        // 🔥 request.type() 으로 변경
        Long noticeId = noticeCommandUseCase.registerNotice(new CreateNoticeCommand(
                request.title(),
                request.content(),
                request.type()
        ));
        CreateNoticeResponse responseData = new CreateNoticeResponse(noticeId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("NOTICE_CREATED", "공지사항 등록에 성공했습니다.", responseData));
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다. (권한: CS_MANAGER)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"NOTICE_NOT_FOUND"})
    public ResponseEntity<Void> deleteNotice(
            @Parameter(description = "삭제할 공지사항 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        noticeCommandUseCase.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modify/{noticeId}")
    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다. (권한: CS_MANAGER)")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {
            "NOTICE_NOT_FOUND", "NOTICE_TYPE_REQUIRED", "TITLE_REQUIRED",
            "TITLE_LENGTH_EXCEEDED", "CONTENT_REQUIRED", "CONTENT_LENGTH_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<UpdateNoticeResponse>> modifyNotice(
            @Parameter(description = "수정할 공지사항 ID", example = "1")
            @PathVariable Long noticeId,

            @Valid @RequestBody UpdateNoticeRequest request
    ) {
        // 🔥 request.type() 으로 변경
        noticeCommandUseCase.modifyNotice(noticeId, new UpdateNoticeCommand(
                request.title(),
                request.content(),
                request.type()
        ));

        UpdateNoticeResponse responseData = new UpdateNoticeResponse(noticeId);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_MODIFIED",
                "공지사항 수정에 성공했습니다.",
                responseData
        ));
    }

    @GetMapping("/{tag}/{index}")
    @Operation(summary = "공지사항 전체 조회", description = "태그와 인덱스(페이지 번호)를 기반으로 공지사항 목록을 페이지네이션 조회합니다. 전체 조회를 원할 경우 tag에 'ALL'을 입력합니다.")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"INVALID_TAG_OR_INDEX"})
    public ResponseEntity<ApiResponse<List<NoticeListResponse>>> getNotices(
            @Parameter(description = "조회할 태그명 (ALL, NOTICE, EVENT, MAINTENANCE 등)", example = "ALL")
            @PathVariable String tag,

            @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1")
            @PathVariable Integer index
    ) {
        List<NoticeListResponse> responseData = noticeQueryUseCase.getNotices(tag, index);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_LIST_FOUND",
                "공지사항 전체 조회에 성공했습니다.",
                responseData
        ));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회", description = "공지사항 ID를 통해 특정 공지사항의 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"NOTICE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @Parameter(description = "조회할 공지사항 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        NoticeResponse responseData = noticeQueryUseCase.getNotice(noticeId);

        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_FOUND",
                "공지사항 상세 조회에 성공했습니다.",
                responseData
        ));
    }
}