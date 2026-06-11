package com.kidmily.algoga_server.benefit.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BenefitErrorCode implements BaseErrorCode {

    COURSE_REWARD_ALREADY_GRANTED(HttpStatus.CONFLICT, "BENEFIT_001", "이미 보상이 지급된 강의입니다."),
    COUPON_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "BENEFIT_002", "쿠폰 정책을 찾을 수 없습니다."),
    INVALID_COUPON_POLICY(HttpStatus.BAD_REQUEST, "BENEFIT_003", "유효하지 않은 쿠폰 정책입니다."),
    MILEAGE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "BENEFIT_004", "마일리지 대상 사용자를 찾을 수 없습니다."),
    INVALID_MILEAGE_AMOUNT(HttpStatus.BAD_REQUEST, "BENEFIT_005", "마일리지 금액은 1 이상이어야 합니다."),
    NOT_ENOUGH_MILEAGE(HttpStatus.BAD_REQUEST, "BENEFIT_006", "보유 마일리지가 부족합니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "BENEFIT_007", "해당 과정을 찾을 수 없습니다."),
    QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "BENEFIT_008", "퀴즈 제출 후 보상을 지급할 수 있습니다."),
    COURSE_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "BENEFIT_009", "강의 이수 내역을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "BENEFIT_010", "해당 국가를 찾을 수 없습니다."),
    BENEFIT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "BENEFIT_011", "로그인이 필요합니다."),
    COURSE_REWARD_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "BENEFIT_012", "강의 보상 지급 가능 기간이 만료되었습니다."),
    DUPLICATED_COUPON_POLICY_NAME(HttpStatus.CONFLICT, "BENEFIT_013", "이미 사용 중인 쿠폰명입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
