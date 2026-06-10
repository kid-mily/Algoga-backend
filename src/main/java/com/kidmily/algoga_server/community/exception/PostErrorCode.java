package com.kidmily.algoga_server.community.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    // 400 - 입력값 검증
    POST_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "POST_000", "잘못된 요청입니다. 입력값을 확인해주세요."),
    POST_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "POST_001", "유효하지 않은 카테고리입니다."),
    POST_CATEGORY_BLANK(HttpStatus.BAD_REQUEST, "POST_002", "카테고리는 필수입니다."),
    POST_FREE_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_003", "자유 태그는 최대 10개까지 등록 가능합니다."),
    POST_COURSE_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_004", "수강강의 태그는 최대 10개까지 등록 가능합니다."),
    POST_FREE_TAG_DUPLICATED(HttpStatus.BAD_REQUEST, "POST_005", "자유 태그에 중복된 값이 있습니다."),
    POST_TITLE_BLANK(HttpStatus.BAD_REQUEST, "POST_006", "제목은 필수입니다."),
    POST_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "POST_007", "본문은 필수입니다."),
    REPORT_DETAIL_TOO_LONG(HttpStatus.BAD_REQUEST, "POST_020", "신고 상세 내용은 최대 255자입니다."),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "POST_023", "대댓글에는 댓글을 달 수 없습니다."),
    POST_FREE_TAG_TOO_LONG(HttpStatus.BAD_REQUEST, "POST_024", "자유 태그는 최대 10자입니다."),

    // 401 - 인증
    POST_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST_008", "게시글 작성 권한이 없습니다."),
    COMMENT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST_009", "댓글 작성 권한이 없습니다."),
    REACTION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST_010", "좋아요/싫어요 권한이 없습니다."),
    REPORT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "POST_022", "신고 권한이 없습니다."),

    // 409
    REPORT_DUPLICATED(HttpStatus.CONFLICT, "POST_021", "이미 신고한 대상입니다."),

    // 413 - 이미지
    POST_IMAGE_COUNT_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "POST_011", "이미지는 최대 10장까지 업로드 가능합니다."),
    POST_IMAGE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "POST_012", "이미지 한 장의 크기는 최대 10MB입니다."),

    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_013", "게시글을 수정할 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_014", "게시글을 삭제할 권한이 없습니다."),
    POST_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_015", "해당 목록을 조회할 권한이 없습니다."),
    COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_016", "댓글을 수정할 권한이 없습니다."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_017", "댓글을 삭제할 권한이 없습니다."),
    // 404
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_018", "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_019", "댓글을 찾을 수 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}