package com.kidmily.algoga_server.global.infrastructure.s3;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Settings s3Settings;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) return null;

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // 파일명 중복 방지를 위한 UUID 적용. 이 key가 그대로 DB에 저장된다.
        String key = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 상대경로(object key)만 반환. 절대 URL은 응답 직렬화 시점에 CDN 루트로 매핑된다.
            return key;

        } catch (Exception e) {
            // putObject는 S3Exception(RuntimeException)을 던지므로 IOException만 잡으면 진짜 원인이
            // 로그에 안 남는다. AccessDenied/NoSuchBucket 등 실제 원인을 남기기 위해 전체를 잡아 로깅한다.
            log.error("[S3 Upload Error] 파일 업로드 실패: bucket={}, key={}, cause={}",
                    s3Settings.getBucket(), key, e.toString(), e);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String key) {
        if (key == null || key.isBlank()) return;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(key)
                    .build());
            log.info("[S3 Delete] 파일 삭제 완료: 버킷={}, 키={}", s3Settings.getBucket(), key);
        } catch (Exception e) {
            log.error("[S3 Delete Error] 파일 삭제 중 오류 발생: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFileAsync(java.io.File file, String targetKey) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Settings.getBucket())
                    .key(targetKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            return targetKey;
        } catch (Exception e) {
            log.error("[S3 Async Upload Error] 비동기 파일 업로드 실패: bucket={}, key={}, cause={}",
                    s3Settings.getBucket(), targetKey, e.toString(), e);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }
}
