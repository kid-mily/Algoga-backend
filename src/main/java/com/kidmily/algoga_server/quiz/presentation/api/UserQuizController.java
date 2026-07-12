package com.kidmily.algoga_server.quiz.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizAnswerCommand;
import com.kidmily.algoga_server.quiz.application.command.SubmitQuizCommand;
import com.kidmily.algoga_server.quiz.application.result.QuizSubmitResult;
import com.kidmily.algoga_server.quiz.application.usecase.QuizUseCase;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.quiz.presentation.request.SubmitQuizRequest;
import com.kidmily.algoga_server.quiz.presentation.response.QuizSubmissionResponse;
import com.kidmily.algoga_server.quiz.presentation.response.QuizSubmitResponse;
import com.kidmily.algoga_server.quiz.presentation.response.UserQuizResponse;
import com.kidmily.algoga_server.learning.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "퀴즈", description = "사용자 퀴즈 조회, 제출, 결과 조회 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/quiz")
@RequiredArgsConstructor
public class UserQuizController {

    private final QuizUseCase quizUseCase;

    @Operation(
            summary = "사용자 퀴즈 조회",
            description = """
                    사용자가 강의의 모든 챕터를 완료하면 퀴즈 목록을 조회합니다.
                    정답 번호와 해설은 응답에 포함하지 않습니다.
                    """
    )
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QUIZ_NOT_FOUND",
            "QUIZ_LOCKED",
            "NOT_ENROLLED"
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserQuizResponse>>> getQuizzes(
            @Parameter(description = "강의 ID", example = "53")
            @PathVariable Long courseId,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        List<UserQuizResponse> response = quizUseCase.getQuizzes(
                        currentUserId,
                        courseId
                )
                .stream()
                .map(UserQuizResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "USER_QUIZZES_FOUND",
                        "퀴즈 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "사용자 퀴즈 제출 및 채점",
            description = """
                    사용자가 제출한 퀴즈 답안을 자동 채점합니다.
                    전체 문제 수, 정답 수, 점수, 오답 해설을 반환합니다.
                    """
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QUIZ_NOT_FOUND",
            "QUIZ_LOCKED",
            "INVALID_QUIZ_SUBMISSION",
            "NOT_ENROLLED"
    })
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizSubmitResponse>> submitQuiz(
            @Parameter(description = "강의 ID", example = "53")
            @PathVariable Long courseId,

            @Valid @RequestBody SubmitQuizRequest request,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        List<SubmitQuizAnswerCommand> answers = request.answers()
                .stream()
                .map(answer -> new SubmitQuizAnswerCommand(
                        answer.quizId(),
                        answer.selectedOption()
                ))
                .toList();

        SubmitQuizCommand command = new SubmitQuizCommand(
                currentUserId,
                courseId,
                answers
        );

        QuizSubmitResult result = quizUseCase.submitQuiz(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "QUIZ_SUBMITTED",
                        "퀴즈 제출 및 채점에 성공했습니다.",
                        QuizSubmitResponse.from(result)
                )
        );
    }

    @Operation(
            summary = "내 퀴즈 제출 결과 조회",
            description = """
                    로그인한 사용자의 해당 강의 퀴즈 제출 결과를 조회합니다.
                    현재 저장 구조상 전체 문제 수, 정답 수, 점수, 제출 일시를 반환합니다.
                    """
    )
    @ApiErrorCodeExample(domain = LearningErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "QUIZ_NOT_SUBMITTED",
            "NOT_ENROLLED"
    })
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> getMyQuizSubmission(
            @Parameter(description = "강의 ID", example = "53")
            @PathVariable Long courseId,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveLoginRequired(userDetails);

        QuizSubmissionResponse response = QuizSubmissionResponse.from(
                quizUseCase.getMyQuizSubmission(currentUserId, courseId)
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "MY_QUIZ_SUBMISSION_FOUND",
                        "내 퀴즈 제출 결과 조회에 성공했습니다.",
                        response
                )
        );
    }
}