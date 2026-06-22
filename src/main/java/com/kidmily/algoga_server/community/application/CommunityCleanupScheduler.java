package com.kidmily.algoga_server.community.application;

import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataCommentRepository;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityCleanupScheduler {

    private final SpringDataPostRepository postRepository;
    private final SpringDataCommentRepository commentRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpExpiredPosts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(14);

        var expiredPosts = postRepository.findByIsDeletedTrueAndDeletedAtBefore(threshold);
        if (!expiredPosts.isEmpty()) {
            postRepository.deleteAll(expiredPosts);
            log.info("[배치 작업] 14일 경과한 삭제 게시글 {}건 영구 삭제 완료", expiredPosts.size());
        }

        var expiredComments = commentRepository.findByIsDeletedTrueAndDeletedAtBefore(threshold);
        if (!expiredComments.isEmpty()) {
            commentRepository.deleteAll(expiredComments);
            log.info("[배치 작업] 14일 경과한 삭제 댓글 {}건 영구 삭제 완료", expiredComments.size());
        }
    }
}