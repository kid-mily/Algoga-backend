package com.kidmily.algoga_server.blacklist.presentation.api;

import com.kidmily.algoga_server.blacklist.application.command.DeregisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.command.RegisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUserDetailQuery;
import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUsersQuery;
import com.kidmily.algoga_server.blacklist.application.usecase.BlacklistCommandUseCase;
import com.kidmily.algoga_server.blacklist.application.usecase.BlacklistQueryUseCase;
import com.kidmily.algoga_server.blacklist.exception.BlacklistErrorCode;
import com.kidmily.algoga_server.blacklist.presentation.api.request.RegisterBlacklistRequest;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistUserDetailResponse;
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
@PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
@Tag(name = "Admin Blacklist", description = "관리자 전용 블랙리스트 조회 및 관리 API")
public class AdminBlacklistController {

    private final BlacklistCommandUseCase commandUseCase;

    // 🌟 수정됨: 서비스 구현체(BlacklistAdminQueryService) 대신 추상화된 UseCase 인터페이스를 주입받습니다.
    private final BlacklistQueryUseCase queryUseCase;

    @Operation(summary = "현재 블랙리스트에 등록된 유저 목록 조회", description = "현재 블랙리스트 상태인 유저들의 목록을 아이디/닉네임/이메일로 검색 및 페이징하여 조회합니다.")
    @GetMapping
    public ApiResponse<PageResponse<BlacklistUserDetailResponse>> getBlacklistedList(
            @Parameter(description = "검색어 (아이디, 닉네임, 이메일 포함 검색)", example = "baduser") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        // 🌟 수정됨: 3개의 파라미터를 Query 객체로 포장합니다.
        GetBlacklistUsersQuery query = GetBlacklistUsersQuery.of(keyword, page, size);
        PageResponse<BlacklistUserDetailResponse> response = queryUseCase.getBlacklistedUserList(query);
        return ApiResponse.success("BLACKLIST_LIST_SUCCESS", "현재 블랙리스트에 등록된 유저 목록 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "블랙리스트 후보 유저 전체 목록 페이징 조회", description = "신고 횟수가 5회 이상인 유저들의 목록을 아이디/닉네임/이메일로 검색 및 페이징하여 조회합니다.")
    @GetMapping("/candidates")
    public ApiResponse<PageResponse<BlacklistUserDetailResponse>> getCandidateList(
            @Parameter(description = "검색어 (아이디, 닉네임, 이메일 포함 검색)", example = "baduser") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        // 🌟 수정됨: 3개의 파라미터를 Query 객체로 포장합니다.
        GetBlacklistUsersQuery query = GetBlacklistUsersQuery.of(keyword, page, size);
        PageResponse<BlacklistUserDetailResponse> response = queryUseCase.getCandidateUserList(query);
        return ApiResponse.success("BLACKLIST_CANDIDATES_SUCCESS", "블랙리스트 후보 목록을 성공적으로 조회했습니다.", response);
    }

    @Operation(summary = "단일 유저 상세 조회 (후보 및 블랙리스트 공통)", description = "특정 유저 1명의 정보(유저 정보, 누적 신고 횟수, 현재 블랙리스트 여부)를 상세 조회합니다.")
    @ApiErrorCodeExample(domain = BlacklistErrorCode.class, value = {"CANDIDATE_NOT_FOUND"})
    @GetMapping("/users/{userId}")
    public ApiResponse<BlacklistUserDetailResponse> getUserDetail(
            @Parameter(description = "조회할 유저 ID", example = "105") @PathVariable Long userId
    ) {
        // 🌟 수정됨: userId를 Query 객체로 포장합니다.
        GetBlacklistUserDetailQuery query = GetBlacklistUserDetailQuery.of(userId);
        BlacklistUserDetailResponse response = queryUseCase.getUserDetail(query);
        return ApiResponse.success("BLACKLIST_USER_DETAIL_SUCCESS", "유저 상세 정보를 성공적으로 조회했습니다.", response);
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
        // Command 객체 생성 및 전달 (유지)
        RegisterBlacklistCommand command = RegisterBlacklistCommand.of(userId, request.reason());
        commandUseCase.registerBlacklist(command);
        return ApiResponse.success("BLACKLIST_REGISTER_SUCCESS", "해당 유저를 블랙리스트에 성공적으로 등록했습니다.");
    }

    @Operation(summary = "유저 블랙리스트 해제", description = "블랙리스트에 등록된 유저의 상태를 해제하여 다시 서비스 이용 및 로그인이 가능하도록 조치합니다.")
    @ApiErrorCodeExample(domain = BlacklistErrorCode.class, value = {"BLACKLIST_NOT_FOUND"})
    @PatchMapping("/{userId}/deregister")
    public ApiResponse<Void> deregisterBlacklist(
            @Parameter(description = "블랙리스트를 해제할 유저 ID", example = "105") @PathVariable Long userId
    ) {
        // Command 객체 생성 및 전달 (유지)
        DeregisterBlacklistCommand command = DeregisterBlacklistCommand.of(userId);
        commandUseCase.deregisterBlacklist(command);
        return ApiResponse.success("BLACKLIST_DEREGISTER_SUCCESS", "유저의 블랙리스트 등록이 해제되었습니다.");
    }
}