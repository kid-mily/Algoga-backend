package com.kidmily.algoga_server.lms.infrastructure.s3;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.port.CourseFileStoragePort;
import com.kidmily.algoga_server.lms.application.port.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Slf4j
@Component
public class CourseS3StorageAdapter implements CourseFileStoragePort {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    public CourseS3StorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(UploadFile file, String bucketName, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.originalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String savedFilename = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(savedFilename)
                    .contentType(file.contentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.inputStream(), file.size())
            );

            return endpoint + "/" + bucketName + "/" + savedFilename;
        } catch (RuntimeException exception) {
            log.error("[Course S3 Upload Error] file upload failed: {}", exception.getMessage(), exception);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String prefix = endpoint + "/" + bucketName + "/";
            if (fileUrl.startsWith(prefix)) {
                String key = fileUrl.substring(prefix.length());

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());

                log.info("[Course S3 Delete] file deleted: bucket={}, key={}", bucketName, key);
            }
        } catch (Exception exception) {
            log.error("[Course S3 Delete Error] file delete failed: {}", exception.getMessage(), exception);
        }
    }
}
