package com.kidmily.algoga_server.inquiry.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQ_001", "해당 문의 내역을 찾을 수 없습니다."),
    INVALID_INQUIRY_QUESTION(HttpStatus.BAD_REQUEST, "INQ_002", "문의 내용이 유효하지 않거나 너무 짧습니다."),
    ALREADY_ANSWERED_INQUIRY(HttpStatus.BAD_REQUEST, "INQ_003", "이미 답변이 완료되어 수정할 수 없는 문의입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}