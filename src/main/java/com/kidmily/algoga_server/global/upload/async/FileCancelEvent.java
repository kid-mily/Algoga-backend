package com.kidmily.algoga_server.global.upload.async;

public record FileCancelEvent(
        String bucketName,
        String trackingId,
        String fileUrl
) {}