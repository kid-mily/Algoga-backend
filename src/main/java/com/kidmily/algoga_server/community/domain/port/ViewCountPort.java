package com.kidmily.algoga_server.community.domain.port;

import java.util.Set;

public interface ViewCountPort {
    void increment(Long postId);
    long getCurrentCount(Long postId);
    long getAndReset(Long postId);
    Set<String> getChangedPostIds();
    void clearChangedPostIds();

    // ▼ 추가: 6시간 내 첫 조회면 true 반환(그리고 마킹), 이미 봤으면 false
    boolean markViewedIfAbsent(Long postId, String viewerKey);
}