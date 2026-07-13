package com.kidmily.algoga_server.global.infrastructure.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3BucketInitializer {

    private final S3Client s3Client;
    private final S3Settings s3Settings;

    @PostConstruct
    public void init() {
        String bucketName = s3Settings.getBucket();
        if (bucketName == null || bucketName.isBlank()) {
            log.info("[S3 Initialize] 버킷명이 비어 있어 초기화를 건너뜁니다.");
            return;
        }
        ensureBucket(bucketName);
    }

    private void ensureBucket(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("[S3 Initialize] '{}' 버킷이 이미 존재합니다.", bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                // MinIO/로컬 환경에서만 자동 생성 시도. AWS는 IAM 권한이 없을 수 있어 실패 시 경고만 남긴다.
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("[S3 Initialize] '{}' 버킷이 자동으로 생성되었습니다! 🎉", bucketName);
                } catch (Exception createException) {
                    log.warn("[S3 Initialize] 버킷 자동 생성 실패(운영은 사전 생성 필요): {}", createException.getMessage());
                }
            } else {
                log.warn("[S3 Initialize] 버킷 확인 중 오류: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("[S3 Initialize] 버킷 확인 중 오류: {}", e.getMessage());
        }
    }
}
