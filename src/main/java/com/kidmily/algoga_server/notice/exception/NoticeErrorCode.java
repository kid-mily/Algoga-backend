package com.kidmily.algoga_server.notice.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements BaseErrorCode {

    // 1. 기본 조회 에러
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NT_001", "공지사항 데이터를 찾을 수 없습니다."),

    // 2. 요청 파라미터 에러
    INVALID_TAG_OR_INDEX(HttpStatus.BAD_REQUEST, "NT_002", "유효하지 않은 태그이거나 인덱스 범위가 잘못되었습니다."),
    INVALID_NOTICE_DATA(HttpStatus.BAD_REQUEST, "NT_003", "유효하지 않은 공지사항 데이터입니다."),

    // 3. 도메인 검증용 에러 (길이/형식)
    TITLE_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "NT_004", "공지사항 제목은 2자 이상 100자 이하여야 합니다."),
    CONTENT_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "NT_005", "공지사항 본문 내용이 허용된 길이를 초과했습니다."),

    // 4. 필수 값 누락 에러 (비어있는 경우)
    TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "NT_006", "공지사항 제목은 필수입니다."),
    CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "NT_007", "공지사항 본문 내용은 필수입니다."),
    NOTICE_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "NT_008", "공지사항 태그(타입)는 필수입니다."),

    // 5. 권한/비즈니스 에러
    UNAUTHORIZED_MANAGER(HttpStatus.FORBIDDEN, "NT_009", "해당 공지사항을 수정 또는 삭제할 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}