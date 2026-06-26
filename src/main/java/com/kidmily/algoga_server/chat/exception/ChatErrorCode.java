package com.kidmily.algoga_server.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHAT_002", "이미 존재하는 채팅방입니다."),
    CHAT_NOT_MEMBER(HttpStatus.FORBIDDEN, "CHAT_003", "채팅방 멤버가 아닙니다."),
    CHAT_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "CHAT_004", "메시지 내용은 필수입니다."),
    CHAT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "CHAT_005", "메시지는 300자 이내여야 합니다."),
    CHAT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_006", "존재하지 않는 유저입니다."),
    CHAT_ROOM_NAME_BLANK(HttpStatus.BAD_REQUEST, "CHAT_007", "채팅방 이름은 필수입니다."),
    CHAT_ROOM_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "CHAT_008", "채팅방 이름은 20자 이내여야 합니다."),
    CHAT_GROUP_MIN_MEMBERS(HttpStatus.BAD_REQUEST, "CHAT_009", "그룹 채팅방은 2명 이상 초대해야 합니다."),
    CHAT_DIRECT_RENAME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHAT_010", "1:1 채팅방은 이름을 변경할 수 없습니다."),
    CHAT_GROUP_MAX_MEMBERS(HttpStatus.BAD_REQUEST, "CHAT_011", "그룹 채팅방은 최대 100명까지 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}