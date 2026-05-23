package com.kidmily.algoga_server.community.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    // 400 - 입력값 검증
    POST_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "POST_001", "유효하지 않은 카테고리입니다."),
    POST_CATEGORY_BLANK(HttpStatus.BAD_REQUEST, "POST_00X", "카테고리는 필수입니다."),
    POST_FREE_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_002", "자유 태그는 최대 10개까지 등록 가능합니다."),
    POST_COURSE_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_003", "수강강의 태그는 최대 10개까지 등록 가능합니다."),
    POST_FREE_TAG_DUPLICATED(HttpStatus.BAD_REQUEST, "POST_00X", "자유 태그에 중복된 값이 있습니다."),
    POST_TITLE_BLANK(HttpStatus.BAD_REQUEST, "POST_007", "제목은 필수입니다."),
    POST_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "POST_008", "본문은 필수입니다."),

    // 401 - 인증
    POST_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST_004", "게시글 작성 권한이 없습니다."),

    // 413 - 이미지
    POST_IMAGE_COUNT_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "POST_005", "이미지는 최대 10장까지 업로드 가능합니다."),
    POST_IMAGE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "POST_006", "이미지 한 장의 크기는 최대 10MB입니다."),

    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_00X", "게시글을 수정할 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_00X", "게시글을 삭제할 권한이 없습니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_00X", "게시글을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}