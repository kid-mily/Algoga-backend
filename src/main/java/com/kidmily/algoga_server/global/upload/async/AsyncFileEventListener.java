package com.kidmily.algoga_server.global.upload.async;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncFileEventListener {

    private final FileStoragePort fileStoragePort;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String CANCEL_PREFIX = "CANCEL_UPLOAD:";

    @Async("taskExecutor")
    @EventListener
    public void handlePreUpload(FilePreUploadEvent event) {
        File tempFile = new File(event.tempFilePath());
        try {
            if (isCanceled(event.trackingId())) return;

            String uploadedKey = fileStoragePort.uploadFileAsync(tempFile, event.targetS3Key());

            if (isCanceled(event.trackingId())) {
                fileStoragePort.deleteFile(uploadedKey);
            }
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void handleCancel(FileCancelEvent event) {
        redisTemplate.opsForValue().set(CANCEL_PREFIX + event.trackingId(), "true", Duration.ofHours(1));
        
        if (event.fileKey() != null && !event.fileKey().isBlank()) {
            fileStoragePort.deleteFile(event.fileKey());
        }
    }

    private boolean isCanceled(String trackingId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CANCEL_PREFIX + trackingId));
    }
}