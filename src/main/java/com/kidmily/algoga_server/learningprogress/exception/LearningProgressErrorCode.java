package com.kidmily.algoga_server.learningprogress.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LearningProgressErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    INVALID_PROGRESS(HttpStatus.BAD_REQUEST, "LMS_003", "유효하지 않은 학습 진도입니다."),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_009", "해당 챕터를 찾을 수 없습니다."),
    CHAPTER_LOCKED(HttpStatus.BAD_REQUEST, "LMS_014", "이전 강의를 먼저 완료해주세요."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "LMS_035", "수강 등록된 강의가 아닙니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
