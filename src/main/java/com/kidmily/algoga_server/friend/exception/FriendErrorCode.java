package com.kidmily.algoga_server.friend.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_001", "존재하지 않는 유저입니다."),
    CANNOT_ADD_SELF(HttpStatus.BAD_REQUEST, "FRIEND_002", "본인을 추가할 수 없습니다."),
    ALREADY_FRIEND(HttpStatus.CONFLICT, "FRIEND_003", "이미 친구입니다."),
    ALREADY_REQUESTED(HttpStatus.CONFLICT, "FRIEND_004", "이미 친구 요청을 보냈거나 대기 중입니다."),
    FRIEND_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "FRIEND_005", "친구는 최대 100명까지 추가할 수 있습니다."),
    ALREADY_BLOCKED(HttpStatus.FORBIDDEN, "FRIEND_006", "차단한 유저입니다. 친구가 되려면 차단 목록에서 차단을 해제해주세요."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_007", "존재하지 않거나 처리할 권한이 없는 친구 요청입니다."),
    RELATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_008", "친구 관계를 찾을 수 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "FRIEND_009", "권한이 없습니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "FRIEND_010", "유효하지 않은 상태입니다."),
    BLOCKED_BY_TARGET(HttpStatus.FORBIDDEN, "FRIEND_011", "상대방이 나를 차단하여 친구 요청을 보낼 수 없습니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "FRIEND_012", "로그인이 필요한 서비스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}