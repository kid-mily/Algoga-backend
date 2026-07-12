package com.kidmily.algoga_server.review.application.scheduler;

import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseReviewDeletionScheduler {

    private final CourseReviewRepository courseReviewRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteHiddenReviewsAfterRetention() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(14);
        courseReviewRepository.deleteHiddenBefore(threshold);
        log.info("[CourseReviewDeletionScheduler] Hidden course reviews older than {} deleted.", threshold);
    }
}
