package com.kidmily.algoga_server.review.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    COURSE_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_019", "강의 이수 내역을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_025", "해당 리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "LMS_026", "이미 해당 강의에 리뷰를 작성했습니다."),
    INVALID_REVIEW_RATING(HttpStatus.BAD_REQUEST, "LMS_027", "리뷰 평점은 1점부터 5점 사이여야 합니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
