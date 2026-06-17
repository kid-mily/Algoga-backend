package com.kidmily.algoga_server.blacklist.application.query;

public record GetBlacklistUserDetailQuery(
        Long userId
) {
    public static GetBlacklistUserDetailQuery of(Long userId) {
        return new GetBlacklistUserDetailQuery(userId);
    }
}