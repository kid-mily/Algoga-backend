package com.kidmily.algoga_server.lms.application.scheduler;

import com.kidmily.algoga_server.lms.application.port.LearningProgressCachePort;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningProgressFlushScheduler {

    private final LearningProgressCachePort learningProgressCachePort;
    private final LearningProgressFlushWriter learningProgressFlushWriter;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${lms.progress.flush-interval-ms:30000}")
    public void flushDirtyProgresses() {
        List<LearningProgress> dirtyProgresses;
        try {
            dirtyProgresses = learningProgressCachePort.findDirtyProgresses();
        } catch (RuntimeException exception) {
            log.warn("[LearningProgressFlush] Failed to read dirty progresses from Redis.", exception);
            meterRegistry.counter("algoga_lms_progress_flush_error_total", "operation", "read").increment();
            return;
        }

        if (dirtyProgresses.isEmpty()) {
            return;
        }

        int flushedCount = 0;
        for (LearningProgress dirtyProgress : dirtyProgresses) {
            try {
                LearningProgress savedProgress = learningProgressFlushWriter.save(dirtyProgress);
                learningProgressCachePort.markFlushed(savedProgress);
                flushedCount++;
            } catch (RuntimeException exception) {
                log.warn("[LearningProgressFlush] Failed to flush progress. userId={}, courseId={}, chapterId={}",
                        dirtyProgress.getUserId(), dirtyProgress.getCourseId(), dirtyProgress.getChapterId(), exception);
                meterRegistry.counter("algoga_lms_progress_flush_error_total", "operation", "write").increment();
            }
        }

        meterRegistry.counter("algoga_lms_progress_flush_total").increment();
        meterRegistry.counter("algoga_lms_progress_flush_item_total").increment(flushedCount);
        log.info("[LearningProgressFlush] Flushed dirty progresses. requested={}, succeeded={}",
                dirtyProgresses.size(), flushedCount);
    }
}
