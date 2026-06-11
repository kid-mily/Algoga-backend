// inquiry/exception/InquiryErrorCode.java
package com.kidmily.algoga_server.inquiry.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQ_001", "해당 문의 내역을 찾을 수 없습니다."),
    INVALID_INQUIRY_TITLE(HttpStatus.BAD_REQUEST, "INQ_002", "문의 제목이 유효하지 않거나 너무 짧습니다."),
    INVALID_INQUIRY_CONTENT(HttpStatus.BAD_REQUEST, "INQ_003", "문의 내용이 유효하지 않거나 너무 짧습니다."),
    ALREADY_ANSWERED_INQUIRY(HttpStatus.BAD_REQUEST, "INQ_004", "이미 답변이 완료되어 수정할 수 없는 문의입니다."),

    // 🌟 신규 추가됨: 도메인 객체 생성 시 유저 ID 검증을 위한 커스텀 에러
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "INQ_005", "사용자 ID는 필수입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}