package com.kidmily.algoga_server.lms.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LmsCacheType {

    // Public course lists are read often and change only through admin course commands.
    // A 10 minute TTL limits stale exposure while protecting DB reads during browsing traffic.
    PUBLIC_COURSE_LIST(Const.PUBLIC_COURSE_LIST, 10 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    public static class Const {
        public static final String PUBLIC_COURSE_LIST = "lmsPublicCourseList";
    }
}