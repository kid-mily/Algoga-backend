package com.kidmily.algoga_server.quiz.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_002", "해당 퀴즈를 찾을 수 없습니다."),
    INVALID_QUIZ_OPTION(HttpStatus.BAD_REQUEST, "LMS_012", "퀴즈 보기는 4개 모두 입력해야 합니다."),
    INVALID_QUIZ_ANSWER(HttpStatus.BAD_REQUEST, "LMS_013", "퀴즈 정답 번호는 1부터 4 사이여야 합니다."),
    QUIZ_LOCKED(HttpStatus.BAD_REQUEST, "LMS_015", "모든 챕터를 완료해야 퀴즈를 풀 수 있습니다."),
    INVALID_QUIZ_SUBMISSION(HttpStatus.BAD_REQUEST, "LMS_016", "퀴즈 제출 답안이 올바르지 않습니다."),
    QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "LMS_018", "퀴즈 제출 내역을 찾을 수 없습니다."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "LMS_035", "수강 등록된 강의가 아닙니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
