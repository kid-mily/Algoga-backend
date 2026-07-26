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
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin-User", description = "관리자 전용 유저 관리 및 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;

    private final FriendQueryUseCase friendQueryUseCase;
    private final PostQueryUseCase postQueryUseCase;
    private final CommentQueryUseCase commentQueryUseCase;

    @Operation(summary = "유저 리스트 전체 조회 (관리자용)")
    @GetMapping
    public ApiResponse<Page<AdminUserListResponse>> getUserList(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<User> users = userService.getAdminUserListRaw(pageable);

        // 친구/게시글/댓글 수 모두 유저마다 따로 부르는 대신, 이 페이지에 있는 유저 ID를 모아
        // 한 번에 배치 조회한다(N+1 방지). 게시글/댓글 배치 메서드는 community 팀에서 추가해준 것.
        List<Long> userIds = users.map(User::getId).toList();
        Map<Long, Long> friendCountByUserId = friendQueryUseCase.countFriendsForUsers(userIds);
        Map<Long, Long> postCountByUserId = postQueryUseCase.countMyPostsForUsers(userIds);
        Map<Long, Long> commentCountByUserId = commentQueryUseCase.countMyCommentsForUsers(userIds);

        Page<AdminUserListResponse> response = users.map(user -> {
            long friendCount = friendCountByUserId.getOrDefault(user.getId(), 0L);
            long postCount = postCountByUserId.getOrDefault(user.getId(), 0L);
            long commentCount = commentCountByUserId.getOrDefault(user.getId(), 0L);

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
}