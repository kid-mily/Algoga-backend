package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.Quiz;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 퀴즈 조회 응답")
public record UserQuizResponse(

        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "강의 ID", example = "3")
        Long courseId,

        @Schema(description = "퀴즈 문제", example = "일본 오사카 여행 전 준비물로 가장 적절한 것은?")
        String question,

        @Schema(description = "1번 보기", example = "여권")
        String option1,

        @Schema(description = "2번 보기", example = "두꺼운 겨울 패딩")
        String option2,

        @Schema(description = "3번 보기", example = "국제운전면허증만")
        String option3,

        @Schema(description = "4번 보기", example = "현지 주민등록증")
        String option4
) {

    public static UserQuizResponse from(Quiz quiz) {
        return new UserQuizResponse(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getQuestion(),
                quiz.getOption1(),
                quiz.getOption2(),
                quiz.getOption3(),
                quiz.getOption4()
        );
    }
}