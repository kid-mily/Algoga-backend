package com.kidmily.algoga_server.community.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityCacheType {

    // 게시글 상세 (핫스팟 트렌딩): 10분
    POST_DETAIL(Const.POST_DETAIL, 10 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    public static class Const {
        public static final String POST_DETAIL = "postDetail";
    }
}