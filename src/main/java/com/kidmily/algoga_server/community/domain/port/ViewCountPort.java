package com.kidmily.algoga_server.community.domain.port;

import java.util.Set;

public interface ViewCountPort {
    void increment(Long postId);
    long getCurrentCount(Long postId);
    long getAndReset(Long postId);
    Set<String> getChangedPostIds();
    void clearChangedPostIds();
}