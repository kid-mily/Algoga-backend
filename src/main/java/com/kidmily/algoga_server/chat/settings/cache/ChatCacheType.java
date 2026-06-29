package com.kidmily.algoga_server.chat.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatCacheType {

    CHAT_ROOMS(Const.CHAT_ROOMS, 5 * 60); // 5분 TTL

    private final String cacheName;
    private final int ttlSeconds;

    public static class Const {
        public static final String CHAT_ROOMS = "chatRooms";
    }
}