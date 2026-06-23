package com.kidmily.algoga_server.notice.settings.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeCacheType {

    // 메인 노출 공지 (조회수 최상): 6시간 (캐시 적중률 극대화, DB 보호)
    NOTICE_MAIN(Const.NOTICE_MAIN, 6 * 60 * 60),

    // 페이징 목록 (무한정 생성 가능성 있음): 10분 (OOM 방지, 빠른 메모리 회수)
    NOTICE_LIST(Const.NOTICE_LIST, 10 * 60),

    // 공지사항 상세 (트렌딩 데이터): 6시간 (자주 보는 글은 6시간 유지, 안 보는 글은 소멸)
    NOTICE_DETAIL(Const.NOTICE_DETAIL, 6 * 60 * 60),

    // 태그 목록 (정적 데이터): 24시간 (사실상 변하지 않음)
    NOTICE_TAGS(Const.NOTICE_TAGS, 24 * 60 * 60);

    private final String cacheName;
    private final int ttlSeconds;

    // 어노테이션(@Cacheable 등)에서 사용할 수 있도록 static final String 상수 정의
    public static class Const {
        public static final String NOTICE_MAIN = "noticeMain";
        public static final String NOTICE_LIST = "noticeList";
        public static final String NOTICE_DETAIL = "noticeDetail";
        public static final String NOTICE_TAGS = "noticeTags";
    }
}