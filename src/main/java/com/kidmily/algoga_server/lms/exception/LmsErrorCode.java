package com.kidmily.algoga_server.lms.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LmsErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 과정을 찾을 수 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_002", "해당 퀴즈를 찾을 수 없습니다."),
    INVALID_PROGRESS(HttpStatus.BAD_REQUEST, "LMS_003", "유효하지 않은 학습 진도입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LMS_004", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_005", "파일을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    CONTINENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_007", "해당 대륙의 국가를 찾을 수 없습니다."),
    INVALID_CONTINENT_CODE(HttpStatus.BAD_REQUEST, "LMS_008", "유효하지 않은 대륙 코드입니다."),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_009", "해당 챕터를 찾을 수 없습니다."),
    CHAPTER_VIDEO_REQUIRED(HttpStatus.BAD_REQUEST, "LMS_010", "챕터 영상 파일은 필수입니다."),
    INVALID_CHAPTER_ORDER(HttpStatus.BAD_REQUEST, "LMS_011", "유효하지 않은 챕터 순서입니다."),
    INVALID_QUIZ_OPTION(HttpStatus.BAD_REQUEST, "LMS_012", "퀴즈 보기는 4개 모두 입력해야 합니다."),
    INVALID_QUIZ_ANSWER(HttpStatus.BAD_REQUEST, "LMS_013", "퀴즈 정답 번호는 1부터 4 사이여야 합니다."),
    CHAPTER_LOCKED(HttpStatus.BAD_REQUEST, "LMS_014", "이전 강의를 먼저 완료해주세요."),
    QUIZ_LOCKED(HttpStatus.BAD_REQUEST, "LMS_015", "모든 챕터를 완료한 후 퀴즈를 풀 수 있습니다."),
    INVALID_QUIZ_SUBMISSION(HttpStatus.BAD_REQUEST, "LMS_016", "퀴즈 제출 답안이 올바르지 않습니다."),
    COURSE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "LMS_017", "이미 이수 완료한 강의입니다."),
    QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "LMS_018", "퀴즈 제출 후 강의를 이수 완료할 수 있습니다."),
    COURSE_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_019", "강의 이수 내역을 찾을 수 없습니다."),
    COURSE_REWARD_ALREADY_GRANTED(HttpStatus.CONFLICT, "LMS_020", "이미 보상이 지급된 강의입니다."),
    COUPON_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_021", "쿠폰 정책을 찾을 수 없습니다."),
    INVALID_COUPON_POLICY(HttpStatus.BAD_REQUEST, "LMS_022", "유효하지 않은 쿠폰 정책입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}