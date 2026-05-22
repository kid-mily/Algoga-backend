package com.kidmily.algoga_server.lms.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LmsErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 과정을 찾을 수 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_002", "해당 퀴즈를 찾을 수 없습니다."),
    INVALID_PROGRESS(HttpStatus.BAD_REQUEST, "LMS_003", "유효하지 않은 학습 진도입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LMS_004", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_005", "파일을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    CONTINENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_007", "해당 대륙의 국가를 찾을 수 없습니다."),
    INVALID_CONTINENT_CODE(HttpStatus.BAD_REQUEST, "LMS_008", "유효하지 않은 대륙 코드입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}