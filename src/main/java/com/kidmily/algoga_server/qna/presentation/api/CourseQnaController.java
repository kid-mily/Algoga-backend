package com.kidmily.algoga_server.qna.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.qna.presentation.request.CreateCourseQnaCommentRequest;
import com.kidmily.algoga_server.qna.presentation.request.CreateCourseQnaRequest;
import com.kidmily.algoga_server.qna.presentation.response.CourseQnaCommentResponse;
import com.kidmily.algoga_server.qna.presentation.response.CourseQnaDetailResponse;
import com.kidmily.algoga_server.qna.presentation.response.CourseQnaResponse;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "강의 Q&A", description = "강의 Q&A 등록, 조회, 상세, 댓글/대댓글 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/qnas")
@RequiredArgsConstructor
public class CourseQnaController {

    private final CourseUseCase courseUseCase;

    @Operation(summary = "강의 Q&A 등록")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PostMapping
    public ResponseEntity<ApiResponse<CourseQnaResponse>> createQna(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Valid @RequestBody CreateCourseQnaRequest request,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        CreateCourseQnaCommand command = new CreateCourseQnaCommand(
                courseId,
                currentUserId,
                request.title(),
                request.question()
        );

        var qna = courseUseCase.createQna(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_QNA_CREATED",
                        "Q&A 등록에 성공했습니다.",
                        CourseQnaResponse.from(qna)
                ));
    }

    @Operation(summary = "강의 Q&A 목록 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseQnaResponse>>> getQnas(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<CourseQnaResponse> response = courseUseCase.getQnas(courseId)
                .stream()
                .map(CourseQnaResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_QNAS_FOUND",
                        "Q&A 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "강의 Q&A 상세 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND"
    })
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<CourseQnaDetailResponse>> getQnaDetail(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId
    ) {
        CourseQnaDetailResult result = courseUseCase.getQnaDetail(courseId, qnaId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_QNA_FOUND",
                        "Q&A 상세 조회에 성공했습니다.",
                        CourseQnaDetailResponse.from(result)
                )
        );
    }

    @Operation(summary = "강의 Q&A 사용자 댓글/대댓글 등록")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND",
            "QNA_COMMENT_NOT_FOUND"
    })
    @PostMapping("/{qnaId}/comments")
    public ResponseEntity<ApiResponse<CourseQnaCommentResponse>> createUserComment(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId,

            @Valid @RequestBody CreateCourseQnaCommentRequest request,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        CreateCourseQnaCommentCommand command = new CreateCourseQnaCommentCommand(
                courseId,
                qnaId,
                request.parentCommentId(),
                currentUserId,
                "USER",
                request.content()
        );

        var comment = courseUseCase.createComment(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_QNA_COMMENT_CREATED",
                        "Q&A 댓글 등록에 성공했습니다.",
                        CourseQnaCommentResponse.from(comment)
                ));
    }
}