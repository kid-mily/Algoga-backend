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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseS3StorageAdapter implements CourseFileStoragePort {

    private final S3Client s3Client;
    private final S3Settings s3Settings;

    @Override
    public String uploadFile(UploadFile file, String directory) {
        return upload(file, directory, null);
    }

    @Override
    public String uploadAttachmentFile(UploadFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return upload(file, directory, attachmentContentDisposition(file.originalFilename()));
    }

    private String upload(UploadFile file, String directory, String contentDisposition) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.originalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String key = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(key)
                    .contentType(file.contentType());

            // 강의자료 등 첨부 파일은 다운로드 시 원본 파일명으로 저장되도록 Content-Disposition을 설정한다.
            if (contentDisposition != null) {
                requestBuilder.contentDisposition(contentDisposition);
            }

            s3Client.putObject(
                    requestBuilder.build(),
                    RequestBody.fromInputStream(file.inputStream(), file.size())
            );

            // 상대경로(object key)만 반환. 절대 URL은 응답 직렬화 시점에 CDN 루트로 매핑된다.
            return key;
        } catch (RuntimeException exception) {
            log.error("[Course S3 Upload Error] file upload failed: {}", exception.getMessage(), exception);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    // 다운로드 시 원본 파일명으로 저장되도록 하는 Content-Disposition 값. 한글 등은 RFC 5987(UTF-8) 방식으로 인코딩한다.
    private String attachmentContentDisposition(String originalFilename) {
        String fileName = (originalFilename == null || originalFilename.isBlank())
                ? "download"
                : originalFilename;
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
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
