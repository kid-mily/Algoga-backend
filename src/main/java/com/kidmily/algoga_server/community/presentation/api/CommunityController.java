package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.command.*;
import com.kidmily.algoga_server.community.application.port.UserPort;
import com.kidmily.algoga_server.community.application.usecase.CommentCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.PostCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.presentation.api.request.*;
import com.kidmily.algoga_server.community.presentation.api.response.*;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class CommunityController {

    private final PostCommandUseCase postCommandUseCase;
    private final PostQueryUseCase postQueryUseCase;
    private final CommentCommandUseCase commentCommandUseCase;
    private final UserPort userPort;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 작성", description = "나라, 자유, 수강강의 태그 및 최대 10장의 사진을 포함해 게시글을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 작성에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_INVALID_REQUEST",
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED",
            "POST_COURSE_TAG_LIMIT_EXCEEDED",
            "POST_IMAGE_COUNT_EXCEEDED",
            "POST_IMAGE_SIZE_EXCEEDED",
            "POST_UNAUTHORIZED"
    })
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @Valid @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        CreatePostCommand command = new CreatePostCommand(
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                request.lectureId(),
                request.freeTags() == null ? List.of() : request.freeTags(),
                request.images() == null ? List.of() : request.images()
        );

        Long createdId = postCommandUseCase.handle(command);
        CreatePostResponse responseData = new CreatePostResponse(createdId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("POST_CREATED", "게시글 작성에 성공했습니다.", responseData));
    }

    @GetMapping("/tags")
    @Operation(summary = "게시글 카테고리 태그 목록 조회", description = "게시글 작성 시 선택 가능한 카테고리 태그 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getCategories() {
        List<TagResponse> responseData = postQueryUseCase.getCategories()
                .stream()
                .map(TagResponse::fromCategory)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("CATEGORIES_FOUND", "카테고리 목록 조회에 성공했습니다.", responseData));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "무한 스크롤 방식으로 게시글 목록을 조회합니다. 카테고리 필터링 가능 (다중 선택 가능).")
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @Parameter(description = "마지막 게시글 ID (첫 페이지는 생략)", example = "420")
            @RequestParam(required = false) Long lastPostId,

            @Parameter(description = "필터링할 카테고리 (다중 선택 가능, 생략 시 전체)", example = "QUESTION,TRAVEL_REVIEW")
            @RequestParam(required = false) List<PostTagType> categories
    ) {
        PostListResponse responseData = postQueryUseCase.getPosts(lastPostId, categories);
        return ResponseEntity.ok(ApiResponse.success("POSTS_FOUND", "게시글 목록 조회에 성공했습니다.", responseData));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 수정", description = "본인 게시글의 제목, 내용, 카테고리, 태그, 새 사진 파일들을 수정합니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_INVALID_REQUEST",
            "POST_UNAUTHORIZED",
            "POST_NOT_FOUND",
            "POST_UPDATE_FORBIDDEN",
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @ModelAttribute UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        UpdatePostCommand command = new UpdatePostCommand(
                postId,
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                request.lectureId(),
                request.freeTags() == null ? List.of() : request.freeTags(),
                request.images() == null ? List.of() : request.images()
        );

        postCommandUseCase.handle(command);
        UpdatePostResponse responseData = new UpdatePostResponse(postId);

        return ResponseEntity.ok(ApiResponse.success("POST_UPDATED", "게시글 수정에 성공했습니다.", responseData));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "본인 게시글을 Soft Delete 처리합니다. 연관 댓글도 함께 삭제됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "게시글이 성공적으로 삭제되었습니다. (반환 바디 없음)")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_NOT_FOUND",          // 404 Not Found (스웨거 명세에 확실하게 추가 완료)
            "POST_DELETE_FORBIDDEN",   // 403 Forbidden (본인 글이 아닐 때)
            "POST_UNAUTHORIZED"        // 401 Unauthorized (로그인 안 했을 때)
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();


        DeletePostCommand command = new DeletePostCommand(postId, currentUserId);
        postCommandUseCase.handle(command);

        return ResponseEntity.noContent().build();
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    @Operation(summary = "게시글 단건 조회", description = "게시글 ID로 상세 정보를 조회합니다. 조회수가 +1 증가합니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {"POST_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        PostResponse responseData = postQueryUseCase.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success("POST_FOUND", "게시글 조회에 성공했습니다.", responseData));
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    @Operation(summary = "댓글/대댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 작성에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_NOT_FOUND",       // 404 존재하지 않는 게시글
            "COMMENT_UNAUTHORIZED"     // 401 비로그인
    })
    public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();


        CreateCommentCommand command = new CreateCommentCommand(
                postId,
                currentUserId,
                request.parentId(),
                request.content()
        );

        Comment savedComment = commentCommandUseCase.handle(command);

        CreateCommentResponse responseData = new CreateCommentResponse(
                savedComment.getCommentId(),
                savedComment.getUserId(),
                userPort.getNickname(savedComment.getUserId()),
                userPort.getProfileImageUrl(savedComment.getUserId()),
                savedComment.getContent(),
                savedComment.getParentId(),
                savedComment.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("COMMENT_CREATED", "댓글 작성에 성공했습니다.", responseData));
    }

}
