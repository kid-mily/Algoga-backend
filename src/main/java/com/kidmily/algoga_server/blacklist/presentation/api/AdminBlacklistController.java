package com.kidmily.algoga_server.blacklist.presentation.api;

import com.kidmily.algoga_server.blacklist.application.command.RegisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.service.BlacklistAdminQueryService;
import com.kidmily.algoga_server.blacklist.application.usecase.BlacklistCommandUseCase;
import com.kidmily.algoga_server.blacklist.exception.BlacklistErrorCode;
import com.kidmily.algoga_server.blacklist.presentation.api.request.RegisterBlacklistRequest;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistCandidateDetailResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/blacklists")
@PreAuthorize("hasAnyRole('SUPER_ADMIN')")
@Tag(name = "Admin Blacklist", description = "관리자 전용 블랙리스트 조회 및 관리 API")
public class AdminBlacklistController {

    private final BlacklistCommandUseCase commandUseCase;
    private final BlacklistAdminQueryService adminQueryService;

    @Operation(summary = "블랙리스트 후보 유저 전체 목록 페이징 조회", description = "신고 횟수가 5회 이상인 유저들의 목록을 페이징하여 조회합니다. (블랙리스트 등록 화면용 데이터)")
    @GetMapping("/candidates")
    public ApiResponse<PageResponse<BlacklistCandidateDetailResponse>> getCandidateList(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<BlacklistCandidateDetailResponse> response = adminQueryService.getCandidateUserList(page, size);
        return ApiResponse.success("BLACKLIST_CANDIDATES_SUCCESS", "블랙리스트 후보 목록을 성공적으로 조회했습니다.", response);
    }

    @Operation(summary = "블랙리스트 후보 단일 유저 상세 조회", description = "신고 5회 이상 유저 1명의 정보(유저 정보, 누적 신고 횟수, 현재 블랙리스트 여부)를 상세 조회합니다.")
    @ApiErrorCodeExample(domain = BlacklistErrorCode.class, value = {"CANDIDATE_NOT_FOUND"})
    @GetMapping("/candidates/{userId}")
    public ApiResponse<BlacklistCandidateDetailResponse> getCandidateDetail(
            @Parameter(description = "조회할 유저 ID", example = "105") @PathVariable Long userId
    ) {
        BlacklistCandidateDetailResponse response = adminQueryService.getCandidateUserDetail(userId);
        return ApiResponse.success("BLACKLIST_CANDIDATE_DETAIL_SUCCESS", "블랙리스트 후보 상세 정보를 성공적으로 조회했습니다.", response);
    }

    @Operation(summary = "유저 블랙리스트 등록", description = "신고 횟수 조건을 검증한 뒤 해당 유저를 블랙리스트에 등록하고, 접속 중인 토큰을 즉시 만료시킵니다.")
    @ApiErrorCodeExample(domain = BlacklistErrorCode.class, value = {
            "ALREADY_BLACKLISTED",
            "NOT_ENOUGH_REPORTS"
    })
    @PostMapping("/{userId}")
    public ApiResponse<Void> registerBlacklist(
            @Parameter(description = "블랙리스트에 등록할 유저 ID", example = "105") @PathVariable Long userId,
            @Valid @RequestBody RegisterBlacklistRequest request) {
        commandUseCase.registerBlacklist(RegisterBlacklistCommand.of(userId, request.reason()));
        return ApiResponse.success("BLACKLIST_REGISTER_SUCCESS", "해당 유저를 블랙리스트에 성공적으로 등록했습니다.");
    }

    @Operation(summary = "유저 블랙리스트 해제", description = "블랙리스트에 등록된 유저의 상태를 해제하여 다시 서비스 이용 및 로그인이 가능하도록 조치합니다.")
    @ApiErrorCodeExample(domain = BlacklistErrorCode.class, value = {"BLACKLIST_NOT_FOUND"})
    @PatchMapping("/{userId}/deregister")
    public ApiResponse<Void> deregisterBlacklist(
            @Parameter(description = "블랙리스트를 해제할 유저 ID", example = "105") @PathVariable Long userId
    ) {
        commandUseCase.deregisterBlacklist(userId);
        return ApiResponse.success("BLACKLIST_DEREGISTER_SUCCESS", "유저의 블랙리스트 등록이 해제되었습니다.");
    }
}