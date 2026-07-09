package com.kidmily.algoga_server.passport.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PassportErrorCode implements BaseErrorCode {

    PASSPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "PASS_001", "등록된 여권 정보를 찾을 수 없습니다."),
    PASSPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PASS_002", "이미 등록된 여권 정보가 있습니다. 수정 기능을 이용해주세요."),
    INVALID_PASSPORT_FIELD(HttpStatus.BAD_REQUEST, "PASS_003", "여권 정보 값이 올바르지 않습니다."),
    INVALID_EXPIRY_DATE(HttpStatus.BAD_REQUEST, "PASS_004", "여권 만료일은 발급일 이후여야 합니다."),
    INVALID_SEX_CODE(HttpStatus.BAD_REQUEST, "PASS_005", "성별 코드는 M, F, X 중 하나여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
