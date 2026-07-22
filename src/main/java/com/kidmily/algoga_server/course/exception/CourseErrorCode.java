package com.kidmily.algoga_server.course.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LMS_004", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_005", "파일을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_009", "해당 챕터를 찾을 수 없습니다."),
    CHAPTER_VIDEO_REQUIRED(HttpStatus.BAD_REQUEST, "LMS_010", "챕터 영상 파일은 필수입니다."),
    INVALID_CHAPTER_ORDER(HttpStatus.BAD_REQUEST, "LMS_011", "챕터 순서는 1부터 5 사이여야 합니다."),
    QUIZ_LOCKED(HttpStatus.BAD_REQUEST, "LMS_015", "모든 챕터를 완료해야 퀴즈를 풀 수 있습니다."),
    COURSE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "LMS_017", "이미 수강 완료한 강의입니다."),
    QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "LMS_018", "퀴즈 제출 내역을 찾을 수 없습니다."),
    CHAPTER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "LMS_032", "챕터는 최대 5개까지만 등록할 수 있습니다."),
    DUPLICATED_CHAPTER_ORDER(HttpStatus.BAD_REQUEST, "LMS_033", "이미 사용 중인 챕터 순서입니다."),
    INVALID_COURSE_LEVEL(HttpStatus.BAD_REQUEST, "LMS_034", "강의 레벨은 BEGINNER, INTERMEDIATE, ADVANCED 중 하나여야 합니다."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "LMS_035", "수강 등록된 강의가 아닙니다."),
    INVALID_COURSE_REWARD_MILEAGE(HttpStatus.BAD_REQUEST, "LMS_040", "강의 최대 지급 마일리지는 0 이상이어야 합니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다."),
    INVALID_COURSE_FILE_TYPE(HttpStatus.BAD_REQUEST, "LMS_042", "허용되지 않는 강의자료 형식입니다. 문서 파일(PDF, Word, PPT, Excel, HWP, TXT)만 업로드할 수 있습니다."),
    COURSE_INCOMPLETE(HttpStatus.BAD_REQUEST, "COURSE_INCOMPLETE", "챕터와 퀴즈가 각각 1개 이상 등록되어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
