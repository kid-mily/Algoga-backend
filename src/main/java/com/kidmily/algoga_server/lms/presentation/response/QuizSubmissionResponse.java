package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.application.result.QuizSubmissionAnswerResult;
import com.kidmily.algoga_server.lms.application.result.QuizSubmissionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "퀴즈 제출 결과 조회 응답")
public record QuizSubmissionResponse(

        @Schema(description = "퀴즈 제출 결과 ID", example = "1")
        Long submissionId,

        @Schema(description = "사용자 ID", example = "2")
        Long userId,

        @Schema(description = "강의 ID", example = "53")
        Long courseId,

        @Schema(description = "전체 문제 수", example = "5")
        int totalCount,

        @Schema(description = "정답 개수", example = "4")
        int correctCount,

        @Schema(description = "점수", example = "80")
        int score,

        @Schema(description = "제출 일시", example = "2026-06-24T13:30:00")
        LocalDateTime submittedAt,

        @Schema(description = "문제별 제출 답안 및 해설 목록")
        List<AnswerResponse> answers
) {
    public static QuizSubmissionResponse from(QuizSubmissionResult result) {
        return new QuizSubmissionResponse(
                result.submissionId(),
                result.userId(),
                result.courseId(),
                result.totalCount(),
                result.correctCount(),
                result.score(),
                result.submittedAt(),
                result.answers().stream()
                        .map(AnswerResponse::from)
                        .toList()
        );
    }

    @Schema(description = "문제별 제출 답안 및 해설")
    public record AnswerResponse(
            @Schema(description = "퀴즈 제출 답안 ID", example = "1")
            Long answerId,

            @Schema(description = "퀴즈 ID", example = "10")
            Long quizId,

            @Schema(description = "문제", example = "대한민국의 수도는 어디인가요?")
            String question,

            @Schema(description = "1번 보기", example = "서울")
            String option1,

            @Schema(description = "2번 보기", example = "부산")
            String option2,

            @Schema(description = "3번 보기", example = "대구")
            String option3,

            @Schema(description = "4번 보기", example = "제주")
            String option4,

            @Schema(description = "사용자가 선택한 보기 번호", example = "1")
            int selectedOption,

            @Schema(description = "사용자가 선택한 답안 텍스트", example = "서울")
            String selectedAnswer,

            @Schema(description = "정답 보기 번호", example = "1")
            int correctOption,

            @Schema(description = "정답 텍스트", example = "서울")
            String correctAnswer,

            @Schema(description = "정답 여부", example = "true")
            boolean correct,

            @Schema(description = "해설", example = "대한민국의 수도는 서울입니다.")
            String explanation
    ) {
        private static AnswerResponse from(QuizSubmissionAnswerResult answer) {
            return new AnswerResponse(
                    answer.answerId(),
                    answer.quizId(),
                    answer.question(),
                    answer.option1(),
                    answer.option2(),
                    answer.option3(),
                    answer.option4(),
                    answer.selectedOption(),
                    answer.selectedAnswer(),
                    answer.correctOption(),
                    answer.correctAnswer(),
                    answer.correct(),
                    answer.explanation()
            );
        }
    }
}