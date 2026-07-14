package com.kidmily.algoga_server.course.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseCacheType {

    // 공개 강좌 목록은 자주 조회되지만, 관리자용 강좌 명령을 통해서만 변경됩니다.
    // 10분의 TTL(유효 기간)을 설정하여 탐색 트래픽 발생 시 DB 읽기 부하를 줄이는 동시에, 오래된 정보가 노출되는 것을 방지합니다.
    PUBLIC_COURSE_LIST(Const.PUBLIC_COURSE_LIST, 10 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    public static class Const {
        public static final String PUBLIC_COURSE_LIST = "lmsPublicCourseList";
    }
}