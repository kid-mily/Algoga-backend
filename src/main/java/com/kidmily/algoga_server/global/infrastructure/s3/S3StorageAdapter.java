package com.kidmily.algoga_server.global.infrastructure.s3;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class S3StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;

    // 🔥 버킷은 파라미터로 받으므로 엔드포인트 설정만 남깁니다.
    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    // 브라우저에 노출할 공개 접근 주소. 내부 endpoint(MinIO localhost 등)와 다를 수 있어 분리한다.
    // 미설정 시 endpoint와 동일하게 동작한다.
    @Value("${cloud.aws.s3.public-url:${cloud.aws.s3.endpoint}}")
    private String publicUrl;

    public S3StorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName, String directory) {
        if (file == null || file.isEmpty()) return null;

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // 파일명 중복 방지를 위한 UUID 적용
        String savedFilename = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName) // 파라미터로 받은 명시적 버킷 사용
                    .key(savedFilename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 저장된 이미지의 공개 접근 URL 반환 (브라우저에서 로드 가능한 주소)
            return publicUrl + "/" + bucketName + "/" + savedFilename;

        } catch (IOException e) {
            log.error("[S3 Upload Error] 파일 업로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            // URL에서 S3 Key(경로)만 추출.
            // endpoint/public-url이 달라지거나 과거 저장분(구 endpoint 기준)이 섞여도
            // 버킷 경로("/{bucket}/") 기준으로 잘라 키를 안전하게 추출한다.
            String marker = "/" + bucketName + "/";
            int idx = fileUrl.indexOf(marker);
            if (idx >= 0) {
                String key = fileUrl.substring(idx + marker.length());

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName) // 파라미터로 받은 명시적 버킷 사용
                        .key(key)
                        .build());
                log.info("[S3 Delete] 파일 삭제 완료: 버킷={}, 키={}", bucketName, key);
            }
        } catch (Exception e) {
            log.error("[S3 Delete Error] 파일 삭제 중 오류 발생: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFileAsync(java.io.File file, String bucketName, String targetS3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(targetS3Key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            return publicUrl + "/" + bucketName + "/" + targetS3Key;
        } catch (Exception e) {
            log.error("[S3 Async Upload Error] 비동기 파일 업로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }
}