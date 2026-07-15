package com.kidmily.algoga_server.diagnosis.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiagnosisErrorCode implements BaseErrorCode {

    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    DIAGNOSIS_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_036", "진단평가 문제를 찾을 수 없습니다."),
    INVALID_DIAGNOSIS_ANSWER(HttpStatus.BAD_REQUEST, "LMS_037", "진단평가 답안이 올바르지 않습니다."),
    DIAGNOSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_038", "진단평가 결과를 찾을 수 없습니다."),
    DIAGNOSIS_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_039", "진단평가 결과를 저장하려면 로그인이 필요합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
