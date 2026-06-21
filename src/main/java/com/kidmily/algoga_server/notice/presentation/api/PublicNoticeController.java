package com.kidmily.algoga_server.notice.presentation.api;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.notice.application.usecase.NoticeQueryUseCase;
import com.kidmily.algoga_server.notice.exception.NoticeErrorCode;
import com.kidmily.algoga_server.notice.presentation.NoticeTagType;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeListResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeMainResponse;
import com.kidmily.algoga_server.notice.presentation.api.response.NoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/notices") // 🌟 퍼블릭 경로 (토큰 검증 무시)
@RequiredArgsConstructor
@Tag(name = "Public Notice", description = "공지사항 조회 API (로그인 불필요)")
public class PublicNoticeController {

    private final NoticeQueryUseCase noticeQueryUseCase;

    @GetMapping("/main")
    @Operation(summary = "공지사항 메인 조회", description = "메인 페이지에 노출되는 최신 공지사항 5개를 조회합니다.")
    public ResponseEntity<ApiResponse<List<NoticeMainResponse>>> getNoticeMain() {
        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_MAIN_FOUND", "메인 공지사항 조회에 성공했습니다.", noticeQueryUseCase.getNoticeMain()
        ));
    }

    @GetMapping("/tags")
    @Operation(summary = "공지사항 태그 목록 조회", description = "프론트엔드에서 선택할 수 있는 공지사항 태그(타입) 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<NoticeTagType>>> getNoticeTags() {
        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_TAGS_FOUND", "공지사항 태그 목록 조회에 성공했습니다.", noticeQueryUseCase.getAllNoticeTags()
        ));
    }

    @GetMapping("/{tag}/{index}")
    @Operation(summary = "공지사항 전체 조회", description = "태그와 페이지 번호를 기반으로 공지사항 목록을 한 페이지당 10개씩 조회합니다. 스웨거 응답 예시에서 totalPages(최대 인덱스)와 totalElements 등의 페이징 정보를 확인할 수 있습니다.") // 🌟 설명 추가
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"INVALID_TAG_OR_INDEX"})
    public ResponseEntity<ApiResponse<PageResponse<NoticeListResponse>>> getNotices( // 🌟 PageResponse 로 타입 변경
                                                                                     @Parameter(description = "조회할 태그명 (ALL, NOTICE, EVENT, MAINTENANCE 등)", example = "ALL") @PathVariable String tag,
                                                                                     @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1") @PathVariable Integer index
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_LIST_FOUND", "공지사항 전체 조회에 성공했습니다.", noticeQueryUseCase.getNotices(tag, index)
        ));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = NoticeErrorCode.class, value = {"NOTICE_NOT_FOUND"})
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(
            @Parameter(description = "조회할 공지사항 ID", example = "1") @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "NOTICE_FOUND", "공지사항 상세 조회에 성공했습니다.", noticeQueryUseCase.getNotice(noticeId)
        ));
    }
}