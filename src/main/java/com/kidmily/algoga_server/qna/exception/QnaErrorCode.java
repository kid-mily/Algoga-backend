package com.kidmily.algoga_server.qna.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QnaErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_001", "해당 강의를 찾을 수 없습니다."),
    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_023", "해당 Q&A를 찾을 수 없습니다."),
    QNA_ALREADY_ANSWERED(HttpStatus.CONFLICT, "LMS_024", "이미 답변이 등록된 Q&A입니다."),
    QNA_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LMS_028", "해당 Q&A 댓글을 찾을 수 없습니다."),
    NOT_ENROLLED(HttpStatus.FORBIDDEN, "LMS_035", "수강 등록된 강의가 아닙니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LMS_041", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
