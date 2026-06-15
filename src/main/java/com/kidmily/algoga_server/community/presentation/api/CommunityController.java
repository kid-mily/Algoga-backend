package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.community.application.command.*;
import com.kidmily.algoga_server.community.application.port.UserPort;
import com.kidmily.algoga_server.community.application.usecase.CommentCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.PostCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.exception.PostException;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(summary = "게시글 작성", description = "나라, 자유 태그 및 최대 10장의 사진을 포함해 게시글을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 작성에 성공했습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_INVALID_REQUEST",
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED",
            "POST_COURSE_TAG_LIMIT_EXCEEDED",
            "POST_IMAGE_COUNT_EXCEEDED",
            "POST_IMAGE_SIZE_EXCEEDED",
            "ACCOUNT_TYPE_FORBIDDEN"
    })
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @Valid @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
        Long currentUserId = userDetails.getUser().getId();

        CreatePostCommand command = new CreatePostCommand(
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                null,
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

    @GetMapping("/filters")
    @Operation(summary = "게시글 목록 필터 태그 조회",
            description = "게시글 목록 화면에서 사용하는 카테고리 태그 + 인기 나라 태그(게시글 수 상위 5개)를 함께 조회합니다.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getPostFilterTags() {
        List<TagResponse> responseData = postQueryUseCase.getPostFilterTags();
        return ResponseEntity.ok(ApiResponse.success("POST_FILTER_TAGS_FOUND", "필터 태그 목록 조회에 성공했습니다.", responseData));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회",
            description = "무한 스크롤 방식으로 게시글 목록을 조회합니다. 카테고리(다중 선택)와 나라 필터링이 가능하며, 두 조건은 AND로 결합됩니다.")
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @Parameter(description = "마지막 게시글 ID (첫 페이지는 생략)", example = "420")
            @RequestParam(required = false) Long lastPostId,

            @Parameter(description = "필터링할 카테고리 (다중 선택 가능, 생략 시 전체)", example = "QUESTION,TRAVEL_REVIEW")
            @RequestParam(required = false) List<PostTagType> categories,

            @Parameter(description = "필터링할 나라 ID (인기 나라 태그 클릭 시 사용)", example = "1")
            @RequestParam(required = false) Long countryId
    ) {
        PostListResponse responseData = postQueryUseCase.getPosts(lastPostId, categories, countryId);
        return ResponseEntity.ok(ApiResponse.success("POSTS_FOUND", "게시글 목록 조회에 성공했습니다.", responseData));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 수정", description = "본인 게시글의 제목, 내용, 카테고리, 태그, 새 사진 파일들을 수정합니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_INVALID_REQUEST",
            "POST_NOT_FOUND",
            "POST_UPDATE_FORBIDDEN",
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED",
            "ACCOUNT_TYPE_FORBIDDEN"
    })
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @ModelAttribute UpdatePostRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
        Long currentUserId = userDetails.getUser().getId();

        UpdatePostCommand command = new UpdatePostCommand(
                postId,
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                null,
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
            "POST_NOT_FOUND",          // 404 Not Found
            "POST_DELETE_FORBIDDEN",   // 403 Forbidden (본인 글이 아닐 때)
            "ACCOUNT_TYPE_FORBIDDEN"
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
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
            "ACCOUNT_TYPE_FORBIDDEN"    // 403
    })
    public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new PostException(PostErrorCode.ACCOUNT_TYPE_FORBIDDEN);
        }
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


    // 관리자의 유저별 게시글 목록 조회 페이징 방식
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin/users/{userId}")
    @Operation(summary = "유저별 게시글 목록 조회 (관리자용)", description = "CS 관리자가 특정 유저가 작성한 게시글 목록을 페이지 번호 기반으로 조회합니다.")
    public ResponseEntity<ApiResponse<AdminPostListResponse>> getUserPosts(
            @PathVariable Long userId,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") Integer index,
            @CurrentManager Long managerId
    ) {
        AdminPostListResponse responseData = postQueryUseCase.getMyPostsByPage(userId, index, null);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_USER_POSTS_FOUND", "유저별 게시글 목록 조회에 성공했습니다.", responseData));
    }

    // 관리자의 유저별 게시글 상세 조회(조회수 증가 없음)
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/admin/{postId}")
    @Operation(summary = "게시글 상세 조회 (관리자용)", description = "CS 관리자가 게시글 상세 내용을 조회합니다. 조회수는 증가하지 않습니다.")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {"POST_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PostResponse>> getPostForAdmin(
            @PathVariable Long postId,
            @CurrentManager Long managerId
    ) {
        PostResponse responseData = postQueryUseCase.getPostForAdmin(postId);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_POST_FOUND", "게시글 상세 조회에 성공했습니다.", responseData));
    }

    // 관리자의 게시글 삭제 (소프트 딜리트)
    @PreAuthorize("hasAnyRole('CS_MANAGER', 'SUPER_ADMIN')")
    @DeleteMapping("/admin/{postId}")
    @Operation(summary = "게시글 삭제 (관리자용)", description = "CS 관리자가 부적절한 게시글을 Soft Delete 처리합니다. 연관 댓글도 함께 삭제됩니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "게시글이 성공적으로 삭제되었습니다. (반환 바디 없음)")
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {"POST_NOT_FOUND"})
    public ResponseEntity<Void> deletePostByAdmin(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @CurrentManager Long managerId
    ) {
        postCommandUseCase.handle(new AdminDeletePostCommand(postId));
        return ResponseEntity.noContent().build();
    }

}
