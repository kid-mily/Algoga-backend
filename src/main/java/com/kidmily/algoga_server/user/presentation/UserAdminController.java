package com.kidmily.algoga_server.user.presentation;

// 커뮤니티 파트의 UseCase와 Response Import 추가
import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.application.usecase.CommentQueryUseCase;
import com.kidmily.algoga_server.community.presentation.api.response.AdminPostListResponse;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListResponse;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.application.UserService;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.presentation.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Admin-User", description = "관리자 전용 유저 관리 및 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;

    private final FriendQueryUseCase friendQueryUseCase;
    private final PostQueryUseCase postQueryUseCase;
    private final CommentQueryUseCase commentQueryUseCase;

    @Operation(summary = "유저 유입(가입) 경로 통계", description = "가입일 기준 기간 내 유입 경로별 가입자 수를 조회합니다. from/to를 안 넣으면 전체 기간을 조회합니다.")
    @GetMapping("/statistics/signup-paths")
    public ApiResponse<List<SignupPathStatResponse>> getSignupPathStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<SignupPathStatResponse> stats = userService.getSignupPathStats(resolveFrom(from), resolveTo(to));
        return ApiResponse.success("STAT_SIGNUP_PATH_SUCCESS", "유저 가입 경로 통계 조회 성공", stats);
    }

    @Operation(summary = "유저 유입(가입) 경로 통계 CSV 다운로드", description = "가입일 기준 기간 내 유입 경로별 가입자 수를 CSV로 내려받습니다.")
    @GetMapping("/statistics/signup-paths/csv")
    public ResponseEntity<byte[]> getSignupPathStatsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] csv = userService.getSignupPathStatsCsv(resolveFrom(from), resolveTo(to));
        String filename = URLEncoder.encode("유입경로별_가입자수_통계.csv", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @Operation(summary = "유저 리스트 전체 조회 (관리자용)")
    @GetMapping
    public ApiResponse<Page<AdminUserListResponse>> getUserList(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<User> users = userService.getAdminUserListRaw(pageable);

        Page<AdminUserListResponse> response = users.map(user -> {
            long friendCount = friendQueryUseCase.countFriends(user.getId());

            // 팀원이 만든 페이징 조회 메서드(1페이지)를 호출한 뒤, 그 안에 있는 전체 개수(totalElements)만 쏙 빼옵니다!
            long postCount = postQueryUseCase.getMyPostsByPage(user.getId(), 1, null).totalElements();
            long commentCount = commentQueryUseCase.getMyCommentsByPage(user.getId(), 1).totalElements();

            return AdminUserListResponse.of(user, friendCount, postCount, commentCount);
        });

        return ApiResponse.success("ADMIN_USER_LIST_SUCCESS", "유저 목록 조회 성공", response);
    }

    @Operation(summary = "유저 상세 정보 및 탭 조회 (관리자용)")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable("userId") Long userId) {

        User user = userService.getAdminUserDetailRaw(userId);
        boolean isOnline = userService.isUserOnline(user.getEmail());

        List<AdminFriendDetailResponse> friends = friendQueryUseCase.getAdminFriendDetails(userId);

        // 팀원이 만든 목록 데이터 자체(1페이지 기준)를 몽땅 가져옵니다!
        AdminPostListResponse postData = postQueryUseCase.getMyPostsByPage(userId, 1, null);
        AdminCommentListResponse commentData = commentQueryUseCase.getMyCommentsByPage(userId, 1);

        // DTO에 한 번에 묶어서 반환
        AdminUserDetailResponse response = AdminUserDetailResponse.of(user, isOnline, friends, postData, commentData);

        return ApiResponse.success("ADMIN_USER_DETAIL_SUCCESS", "유저 상세 정보 조회 성공", response);
    }

    // from/to 미지정 시 전체 기간을 조회하도록 기본값 처리
    private LocalDateTime resolveFrom(LocalDateTime from) {
        return (from != null) ? from : LocalDateTime.of(2000, 1, 1, 0, 0);
    }

    private LocalDateTime resolveTo(LocalDateTime to) {
        return (to != null) ? to : LocalDateTime.now();
    }
}