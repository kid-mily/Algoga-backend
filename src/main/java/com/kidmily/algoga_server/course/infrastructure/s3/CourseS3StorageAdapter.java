package com.kidmily.algoga_server.course.infrastructure.s3;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.global.infrastructure.s3.S3Settings;
import com.kidmily.algoga_server.course.application.port.CourseFileStoragePort;
import com.kidmily.algoga_server.course.application.port.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseS3StorageAdapter implements CourseFileStoragePort {

    private final S3Client s3Client;
    private final S3Settings s3Settings;

    @Override
    public String uploadFile(UploadFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.originalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String key = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(key)
                    .contentType(file.contentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.inputStream(), file.size())
            );

            // 상대경로(object key)만 반환. 절대 URL은 응답 직렬화 시점에 CDN 루트로 매핑된다.
            return key;
        } catch (RuntimeException exception) {
            log.error("[Course S3 Upload Error] file upload failed: {}", exception.getMessage(), exception);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(key)
                    .build());

            log.info("[Course S3 Delete] file deleted: bucket={}, key={}", s3Settings.getBucket(), key);
        } catch (Exception exception) {
            log.error("[Course S3 Delete Error] file delete failed: {}", exception.getMessage(), exception);
        }
    }
}
