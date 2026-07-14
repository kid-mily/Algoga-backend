package com.kidmily.algoga_server.learning.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_002", "해당 퀴즈를 찾을 수 없습니다."),
    INVALID_PROGRESS(HttpStatus.BAD_REQUEST, "LMS_003", "유효하지 않은 학습 진도입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LMS_004", "파일 저장에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_005", "파일을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_006", "해당 국가를 찾을 수 없습니다."),
    CONTINENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_007", "해당 대륙의 국가를 찾을 수 없습니다."),
    INVALID_CONTINENT_CODE(HttpStatus.BAD_REQUEST, "LMS_008", "유효하지 않은 대륙 코드입니다."),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_009", "해당 챕터를 찾을 수 없습니다."),
    CHAPTER_VIDEO_REQUIRED(HttpStatus.BAD_REQUEST, "LMS_010", "챕터 영상 파일은 필수입니다."),
    INVALID_CHAPTER_ORDER(HttpStatus.BAD_REQUEST, "LMS_011", "챕터 순서는 1부터 5 사이여야 합니다."),
    INVALID_QUIZ_OPTION(HttpStatus.BAD_REQUEST, "LMS_012", "퀴즈 보기는 4개 모두 입력해야 합니다."),
    INVALID_QUIZ_ANSWER(HttpStatus.BAD_REQUEST, "LMS_013", "퀴즈 정답 번호는 1부터 4 사이여야 합니다."),
    CHAPTER_LOCKED(HttpStatus.BAD_REQUEST, "LMS_014", "이전 강의를 먼저 완료해주세요."),
    QUIZ_LOCKED(HttpStatus.BAD_REQUEST, "LMS_015", "모든 챕터를 완료해야 퀴즈를 풀 수 있습니다."),
    INVALID_QUIZ_SUBMISSION(HttpStatus.BAD_REQUEST, "LMS_016", "퀴즈 제출 답안이 올바르지 않습니다."),
    COURSE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "LMS_017", "이미 수강 완료한 강의입니다."),
    QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "LMS_018", "퀴즈 제출 내역을 찾을 수 없습니다."),
    COURSE_COMPLETION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_019", "강의 이수 내역을 찾을 수 없습니다."),

    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_023", "해당 Q&A를 찾을 수 없습니다."),
    QNA_ALREADY_ANSWERED(HttpStatus.CONFLICT, "LMS_024", "이미 답변이 등록된 Q&A입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_025", "해당 리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "LMS_026", "이미 해당 강의에 리뷰를 작성했습니다."),
    INVALID_REVIEW_RATING(HttpStatus.BAD_REQUEST, "LMS_027", "리뷰 평점은 1점부터 5점 사이여야 합니다."),
    QNA_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_028", "해당 Q&A 댓글을 찾을 수 없습니다."),

    CHAPTER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "LMS_032", "챕터는 최대 5개까지만 등록할 수 있습니다."),
    DUPLICATED_CHAPTER_ORDER(HttpStatus.BAD_REQUEST, "LMS_033", "이미 사용 중인 챕터 순서입니다."),
    INVALID_COURSE_LEVEL(HttpStatus.BAD_REQUEST, "LMS_034", "강의 레벨은 BEGINNER, INTERMEDIATE, ADVANCED 중 하나여야 합니다."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "LMS_035", "수강 등록된 강의가 아닙니다."),
    DIAGNOSIS_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_036", "진단평가 문제를 찾을 수 없습니다."),
    INVALID_DIAGNOSIS_ANSWER(HttpStatus.BAD_REQUEST, "LMS_037", "진단평가 답안이 올바르지 않습니다."),
    DIAGNOSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_038", "진단평가 결과를 찾을 수 없습니다."),
    DIAGNOSIS_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_039", "진단평가 결과를 저장하려면 로그인이 필요합니다."),
    INVALID_COURSE_REWARD_MILEAGE(HttpStatus.BAD_REQUEST, "LMS_040", "강의 최대 지급 마일리지는 0 이상이어야 합니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다."),
    INVALID_COURSE_FILE_TYPE(HttpStatus.BAD_REQUEST, "LMS_042", "허용되지 않는 강의자료 형식입니다. 문서 파일(PDF, Word, PPT, Excel, HWP, TXT)만 업로드할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
