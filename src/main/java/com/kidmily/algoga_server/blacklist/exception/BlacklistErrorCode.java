package com.kidmily.algoga_server.blacklist.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BlacklistErrorCode implements BaseErrorCode {
    ALREADY_BLACKLISTED(HttpStatus.CONFLICT, "BLACKLIST_001", "이미 블랙리스트에 등록된 유저입니다."),
    BLACKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "BLACKLIST_002", "현재 블랙리스트에 등록되어 있지 않은 유저입니다."),
    NOT_ENOUGH_REPORTS(HttpStatus.BAD_REQUEST, "BLACKLIST_003", "처리 완료된 신고 횟수가 부족하여(5회 미만) 블랙리스트로 등록할 수 없습니다."),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "BLACKLIST_004", "해당하는 블랙리스트 후보 유저 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}