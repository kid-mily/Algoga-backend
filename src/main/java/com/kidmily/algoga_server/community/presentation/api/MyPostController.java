package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.presentation.api.response.PostListResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 추후 유저 도메인 통합 시 삭제 예정
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class MyPostController {

    private final PostQueryUseCase postQueryUseCase;

    // 마이페이지 - 내가 쓸 글 목록 조회
    @GetMapping("/posts")
    @Operation(summary = "내가 작성한 게시글 목록 조회", description = "마이페이지에서 본인이 작성한 글 목록을 무한 스크롤 방식으로 최신순 조회합니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_UNAUTHORIZED",      // 401 Unauthorized
            "POST_ACCESS_FORBIDDEN",   // 403 권한 부족
            "POST_NOT_FOUND"          // 404 Not Found
    })

    public ResponseEntity<ApiResponse<PostListResponse>> getMyPosts(
            @Parameter(description = "마지막 게시글 ID (첫 페이지는 생략)", example = "420")
            @RequestParam(required = false) Long lastPostId,

            @Parameter(description = "필터링할 카테고리 (다중 선택 가능, 생략 시 전체)", example = "QUESTION,TRAVEL_REVIEW")
            @RequestParam(required = false) List<PostTagType> categories
    ) {
        Long currentUserId = ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUser().getId();

        PostListResponse responseData = postQueryUseCase.getMyPosts(currentUserId, lastPostId, categories);
        return ResponseEntity.ok(ApiResponse.success("MY_POSTS_FOUND", "내가 작성한 게시글 목록 조회에 성공했습니다.", responseData));
    }

}
