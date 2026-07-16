package com.kidmily.algoga_server.user.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 조회 실패 관련
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 계정입니다."),

    // 중복 방지 관련
    ALREADY_EXISTS_PHONE(HttpStatus.CONFLICT, "USER_005", "이미 사용 중인 전화번호입니다."),

    // 인증 및 보안 관련
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_006", "아이디 또는 비밀번호가 틀렸습니다."),
    DELETED_USER(HttpStatus.FORBIDDEN, "USER_007", "탈퇴한 계정입니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "USER_008", "비밀번호 5회 오류로 인해 5분간 계정이 잠겼습니다."),
    SOCIAL_USER_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "USER_013", "소셜 로그인 유저는 비밀번호를 변경할 수 없습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "USER_014", "새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다."),

    // 탈퇴 검증용 에러 코드 2개 추가
    ACTIVE_BOOKING_EXISTS(HttpStatus.BAD_REQUEST, "USER_010", "진행 중인 예약이 있어 탈퇴할 수 없습니다."),
    ACTIVE_REFUND_EXISTS(HttpStatus.BAD_REQUEST, "USER_011", "진행 중인 환불이 있어 탈퇴할 수 없습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}