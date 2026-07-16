package com.kidmily.algoga_server.quiz.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.quiz.application.command.CreateQuizCommand;
import com.kidmily.algoga_server.quiz.application.command.UpdateQuizCommand;
import com.kidmily.algoga_server.quiz.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.quiz.exception.QuizErrorCode;
import com.kidmily.algoga_server.quiz.presentation.request.admin.CreateQuizRequest;
import com.kidmily.algoga_server.quiz.presentation.request.admin.UpdateQuizRequest;
import com.kidmily.algoga_server.quiz.presentation.response.AdminQuizResponse;
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

@Tag(name = "Admin Quiz", description = "콘텐츠 매니저 퀴즈 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizUseCase quizUseCase;

    @Operation(
            summary = "퀴즈 목록 조회",
            description = "특정 강의에 등록된 퀴즈 목록을 조회합니다. 콘텐츠 매니저용 API이므로 정답과 해설도 함께 반환합니다."
    )
    @ApiErrorCodeExample(domain = QuizErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminQuizResponse>>> getQuizzes(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId
    ) {
        List<AdminQuizResponse> response = quizUseCase.getQuizzes(courseId)
                .stream()
                .map(AdminQuizResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_QUIZZES_FOUND",
                        "퀴즈 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "퀴즈 등록",
            description = "특정 강의에 객관식 4지선다 퀴즈를 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = QuizErrorCode.class, value = {"COURSE_NOT_FOUND", "INVALID_QUIZ_OPTION", "INVALID_QUIZ_ANSWER", "QUIZ_LIMIT_EXCEEDED"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminQuizResponse>> createQuiz(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Valid @RequestBody CreateQuizRequest request
    ) {
        CreateQuizCommand command = new CreateQuizCommand(
                courseId,
                request.question(),
                request.option1(),
                request.option2(),
                request.option3(),
                request.option4(),
                request.correctOption(),
                request.explanation()
        );

        var savedQuiz = quizUseCase.createQuiz(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "QUIZ_CREATED",
                        "퀴즈 등록에 성공했습니다.",
                        AdminQuizResponse.from(savedQuiz)
                ));
    }

    @Operation(
            summary = "퀴즈 수정",
            description = "특정 강의에 등록된 퀴즈 문제, 보기, 정답, 해설을 수정합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = QuizErrorCode.class, value = {"COURSE_NOT_FOUND", "QUIZ_NOT_FOUND", "INVALID_QUIZ_OPTION", "INVALID_QUIZ_ANSWER"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<AdminQuizResponse>> updateQuiz(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "퀴즈 ID", example = "1")
            @PathVariable Long quizId,

            @Valid @RequestBody UpdateQuizRequest request
    ) {
        UpdateQuizCommand command = new UpdateQuizCommand(
                request.question(),
                request.option1(),
                request.option2(),
                request.option3(),
                request.option4(),
                request.correctOption(),
                request.explanation()
        );

        var updatedQuiz = quizUseCase.updateQuiz(courseId, quizId, command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "QUIZ_UPDATED",
                        "퀴즈 수정에 성공했습니다.",
                        AdminQuizResponse.from(updatedQuiz)
                )
        );
    }

    @Operation(
            summary = "퀴즈 삭제",
            description = "특정 강의의 퀴즈를 실제 삭제하지 않고 Soft Delete 처리합니다."
    )
    @ApiErrorCodeExample(domain = QuizErrorCode.class, value = {"COURSE_NOT_FOUND", "QUIZ_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @Parameter(description = "퀴즈 ID", example = "1")
            @PathVariable Long quizId
    ) {
        quizUseCase.deleteQuiz(courseId, quizId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "QUIZ_DELETED",
                        "퀴즈 삭제에 성공했습니다."
                )
        );
    }
}
