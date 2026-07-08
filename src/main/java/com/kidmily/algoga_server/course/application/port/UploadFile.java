package com.kidmily.algoga_server.course.application.port;

import java.io.InputStream;

public record UploadFile(
        String originalFilename,
        String contentType,
        long size,
        InputStream inputStream
) {
    public boolean isEmpty() {
        return size <= 0;
    }
}