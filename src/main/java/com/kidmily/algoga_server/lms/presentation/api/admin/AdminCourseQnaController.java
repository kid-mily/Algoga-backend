package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.lms.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.AnswerCourseQnaRequest;
import com.kidmily.algoga_server.lms.presentation.request.CreateCourseQnaCommentRequest;
import com.kidmily.algoga_server.lms.presentation.response.AdminCourseQnaCommentResponse;
import com.kidmily.algoga_server.lms.presentation.response.AdminCourseQnaDetailResponse;
import com.kidmily.algoga_server.lms.presentation.response.AdminCourseQnaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Course Q&A", description = "콘텐츠 매니저 강의 Q&A 조회, 답변, 댓글/대댓글 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/qnas")
@RequiredArgsConstructor
public class AdminCourseQnaController {

    private final CourseUseCase courseUseCase;

    @Operation(summary = "관리자 강의 Q&A 목록 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminCourseQnaResponse>>> getQnas(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<AdminCourseQnaResponse> response = courseUseCase.getQnas(courseId)
                .stream()
                .map(AdminCourseQnaResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_QNAS_FOUND",
                        "관리자 Q&A 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(summary = "관리자 강의 Q&A 상세 조회")
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<AdminCourseQnaDetailResponse>> getQnaDetail(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId
    ) {
        CourseQnaDetailResult result = courseUseCase.getQnaDetail(courseId, qnaId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_QNA_FOUND",
                        "관리자 Q&A 상세 조회에 성공했습니다.",
                        AdminCourseQnaDetailResponse.from(result)
                )
        );
    }

    @Operation(summary = "강의 Q&A 답변 등록")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND",
            "QNA_ALREADY_ANSWERED"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<ApiResponse<AdminCourseQnaResponse>> answerQna(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId,

            @Valid @RequestBody AnswerCourseQnaRequest request,

            @CurrentManager Long managerId
    ) {
        AnswerCourseQnaCommand command = new AnswerCourseQnaCommand(
                courseId,
                qnaId,
                managerId,
                request.answer()
        );

        var qna = courseUseCase.answerQna(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_QNA_ANSWERED",
                        "Q&A 답변 등록에 성공했습니다.",
                        AdminCourseQnaResponse.from(qna)
                )
        );
    }

    @Operation(summary = "강의 Q&A 관리자 댓글/대댓글 등록")
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND",
            "QNA_COMMENT_NOT_FOUND"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{qnaId}/comments")
    public ResponseEntity<ApiResponse<AdminCourseQnaCommentResponse>> createManagerComment(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId,

            @Valid @RequestBody CreateCourseQnaCommentRequest request,

            @CurrentManager Long managerId
    ) {
        CreateCourseQnaCommentCommand command = new CreateCourseQnaCommentCommand(
                courseId,
                qnaId,
                request.parentCommentId(),
                managerId,
                "MANAGER",
                request.content()
        );

        var comment = courseUseCase.createComment(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_QNA_MANAGER_COMMENT_CREATED",
                        "Q&A 관리자 댓글 등록에 성공했습니다.",
                        AdminCourseQnaCommentResponse.from(comment)
                ));
    }
}