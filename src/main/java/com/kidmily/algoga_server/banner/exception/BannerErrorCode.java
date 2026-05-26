package com.kidmily.algoga_server.banner.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BannerErrorCode implements BaseErrorCode {

    // 1. 기본 조회 에러
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "BN_001", "배너 데이터를 찾을 수 없습니다."),

    // 2. 검증 에러
    DATE_INVALID(HttpStatus.BAD_REQUEST, "BN_002", "배너 시작일은 종료일보다 이전이어야 합니다."),
    TEXT_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "BN_003", "배너 설명 텍스트가 허용된 길이를 초과했습니다."),
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "BN_004", "배너 이미지는 필수입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}