package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.lms.application.usecase.CourseQnaUseCase;
import com.kidmily.algoga_server.lms.domain.model.CourseQna;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.AnswerCourseQnaRequest;
import com.kidmily.algoga_server.lms.presentation.request.CreateCourseQnaRequest;
import com.kidmily.algoga_server.lms.presentation.response.CourseQnaResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
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

@Tag(name = "강의 Q&A", description = "강의 Q&A 등록, 조회, 답변 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/qnas")
@RequiredArgsConstructor
public class CourseQnaController {

    private final CourseQnaUseCase courseQnaUseCase;

    @Operation(
            summary = "강의 Q&A 등록",
            description = "로그인한 사용자가 특정 강의에 질문을 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PostMapping
    public ResponseEntity<ApiResponse<CourseQnaResponse>> createQna(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Valid @RequestBody CreateCourseQnaRequest request,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = userDetails.getUser().getId();

        CreateCourseQnaCommand command = new CreateCourseQnaCommand(
                courseId,
                currentUserId,
                request.title(),
                request.question()
        );

        CourseQna qna = courseQnaUseCase.createQna(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_QNA_CREATED",
                        "Q&A 등록에 성공했습니다.",
                        CourseQnaResponse.from(qna)
                ));
    }

    @Operation(
            summary = "강의 Q&A 목록 조회",
            description = "특정 강의에 등록된 Q&A 목록을 조회합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseQnaResponse>>> getQnas(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<CourseQnaResponse> response = courseQnaUseCase.getQnas(courseId)
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
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<ApiResponse<CourseQnaResponse>> answerQna(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "Q&A ID", example = "1")
            @PathVariable Long qnaId,

            @Valid @RequestBody AnswerCourseQnaRequest request
    ) {
        Long currentManagerId = 1L;

        AnswerCourseQnaCommand command = new AnswerCourseQnaCommand(
                courseId,
                qnaId,
                currentManagerId,
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
}