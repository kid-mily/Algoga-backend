package com.kidmily.algoga_server.global.upload.async;

public record FilePreUploadEvent(
        String tempFilePath,
        String targetS3Key,
        String trackingId
) {}
