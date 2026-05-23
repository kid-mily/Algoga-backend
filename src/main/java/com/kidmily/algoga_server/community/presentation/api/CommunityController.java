package com.kidmily.algoga_server.community.presentation.api;

import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;
import com.kidmily.algoga_server.community.application.usecase.PostCommandUseCase;
import com.kidmily.algoga_server.community.application.usecase.PostQueryUseCase;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.presentation.api.request.CreatePostRequest;
import com.kidmily.algoga_server.community.presentation.api.request.UpdatePostRequest;
import com.kidmily.algoga_server.community.presentation.api.response.CreatePostResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import com.kidmily.algoga_server.community.presentation.api.response.UpdatePostResponse;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "Community", description = "커뮤니티 도메인 API")
public class CommunityController {

    private final PostCommandUseCase postCommandUseCase;
    private final PostQueryUseCase postQueryUseCase;

    @PostMapping
    @Operation(summary = "게시글 작성", description = "나라, 자유, 수강강의 태그 및 최대 10장의 사진을 포함해 게시글을 등록합니다.")

    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {
            "INVALID_REQUEST",
            "UNAUTHORIZED"
    })
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED",
            "POST_COURSE_TAG_LIMIT_EXCEEDED",
            "POST_IMAGE_COUNT_EXCEEDED",
            "POST_IMAGE_SIZE_EXCEEDED"
    })

    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request
    ) {
        long currentUserId = 1L;
        CreatePostCommand command = new CreatePostCommand(
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                request.lectureId(),         // freeTags 앞으로
                request.freeTags() == null ? List.of() : request.freeTags(),
                List.of()
        );
                Long createdId = postCommandUseCase.handle(command);
        CreatePostResponse responseData = new CreatePostResponse(createdId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("POST_CREATED", "게시글 작성에 성공했습니다.", responseData));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "본인 게시글의 제목, 내용, 카테고리, 태그를 수정합니다.")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {
            "INVALID_REQUEST",
            "UNAUTHORIZED"
    })
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_NOT_FOUND",
            "POST_FORBIDDEN",
            "POST_CATEGORY_INVALID",
            "POST_FREE_TAG_LIMIT_EXCEEDED"
    })
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        long currentUserId = 1L;  // TODO: Spring Security 적용 후 교체

        UpdatePostCommand command = new UpdatePostCommand(
                postId,
                currentUserId,
                request.category(),
                request.title(),
                request.content(),
                request.countryId(),
                request.lectureId(),
                request.freeTags() == null ? List.of() : request.freeTags()
        );
        postCommandUseCase.handle(command);
        UpdatePostResponse responseData = new UpdatePostResponse(postId);

        return ResponseEntity.ok(ApiResponse.success("POST_UPDATED", "게시글 수정에 성공했습니다.", responseData));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "본인 게시글을 Soft Delete 처리합니다. 연관 댓글도 함께 삭제됩니다.")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {
            "UNAUTHORIZED"
    })
    @ApiErrorCodeExample(domain = PostErrorCode.class, value = {
            "POST_NOT_FOUND",
            "POST_FORBIDDEN"
    })
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        long currentUserId = 1L;  // TODO: Spring Security 적용 후 교체

        DeletePostCommand command = new DeletePostCommand(postId, currentUserId);
        postCommandUseCase.handle(command);

        return ResponseEntity.ok(ApiResponse.success("POST_DELETED", "게시글이 삭제되었습니다.", null));
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
}
