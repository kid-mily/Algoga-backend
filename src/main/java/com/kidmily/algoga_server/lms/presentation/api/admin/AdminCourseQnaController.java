package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseQnaUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.AnswerCourseQnaRequest;
import com.kidmily.algoga_server.lms.presentation.request.CreateCourseQnaCommentRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseQnaCommentResponse;
import com.kidmily.algoga_server.lms.presentation.response.CourseQnaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Course Q&A", description = "콘텐츠 매니저 강의 Q&A 답변 및 댓글 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/qnas")
@RequiredArgsConstructor
public class AdminCourseQnaController {

    private final CourseQnaUseCase courseQnaUseCase;

    @Operation(
            summary = "강의 Q&A 답변 등록",
            description = "콘텐츠 매니저가 특정 Q&A에 답변을 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND",
            "QNA_ALREADY_ANSWERED"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<ApiResponse<CourseQnaResponse>> answerQna(
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

        CourseQna qna = courseQnaUseCase.answerQna(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_QNA_ANSWERED",
                        "Q&A 답변 등록에 성공했습니다.",
                        CourseQnaResponse.from(qna)
                )
        );
    }

    @Operation(
            summary = "강의 Q&A 관리자 댓글 등록",
            description = "콘텐츠 매니저가 특정 Q&A에 댓글을 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QNA_NOT_FOUND"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/{qnaId}/comments")
    public ResponseEntity<ApiResponse<CourseQnaCommentResponse>> createManagerComment(
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
                managerId,
                "MANAGER",
                request.content()
        );

        CourseQnaComment comment = courseQnaUseCase.createComment(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_QNA_MANAGER_COMMENT_CREATED",
                        "Q&A 관리자 댓글 등록에 성공했습니다.",
                        CourseQnaCommentResponse.from(comment)
                ));
    }
}