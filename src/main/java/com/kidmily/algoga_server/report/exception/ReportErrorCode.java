package com.kidmily.algoga_server.report.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {

    REPORT_DETAIL_TOO_LONG(HttpStatus.BAD_REQUEST, "REPORT_001", "신고 상세 내용은 최대 255자입니다."),
    REPORT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "REPORT_002", "신고 권한이 없습니다."),
    REPORT_DUPLICATED(HttpStatus.CONFLICT, "REPORT_003", "이미 신고한 대상입니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_004", "신고 대상을 찾을 수 없습니다."),
    REPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT_005", "본인을 신고할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_006", "신고 내역을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}