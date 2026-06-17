package com.kidmily.algoga_server.blacklist.application.query;

public record GetBlacklistUsersQuery(
        String keyword,
        int page,
        int size
) {
    public static GetBlacklistUsersQuery of(String keyword, int page, int size) {
        return new GetBlacklistUsersQuery(keyword, page, size);
    }
}