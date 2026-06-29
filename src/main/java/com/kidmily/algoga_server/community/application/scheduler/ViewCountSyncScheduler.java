package com.kidmily.algoga_server.community.application.scheduler;

import com.kidmily.algoga_server.community.domain.port.ViewCountPort;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final ViewCountPort viewCountPort;
    private final PostRepository postRepository;

    @Scheduled(fixedDelay = 60000) // 1분마다
    public void sync() {
        Set<String> ids = viewCountPort.getChangedPostIds();
        if (ids == null || ids.isEmpty()) return;

        viewCountPort.clearChangedPostIds();

        for (String id : ids) {
            Long postId = Long.parseLong(id);
            long count = viewCountPort.getAndReset(postId);
            if (count > 0) {
                postRepository.increaseViewCount(postId, count);
                log.info("[ViewCountSync] postId: {} +{}", postId, count);
            }
        }
    }
}