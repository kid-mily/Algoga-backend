package com.kidmily.algoga_server.global.infrastructure.s3;

import com.kidmily.algoga_server.global.port.out.StorageSettings; // 🔥 공통 인터페이스 주입
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3BucketInitializer {

    private final S3Client s3Client;

    // 🔥 핵심: StorageSettings를 구현한 프로젝트 내의 모든 빈(Banner, Notice 등)을 자동으로 다 가져옵니다!
    private final List<StorageSettings> allStorageSettings;

    @PostConstruct
    public void init() {
        if (allStorageSettings == null || allStorageSettings.isEmpty()) {
            log.info("[S3 Initialize] 등록된 스토리지 설정이 없어 초기화를 건너뜁니다.");
            return;
        }

        // 🌟 프로젝트에 등록된 모든 도메인의 버킷 설정을 돌면서 전부 생성해 줍니다.
        for (StorageSettings settings : allStorageSettings) {
            String bucketName = settings.getBucketName();

            if (bucketName != null && !bucketName.isBlank()) {
                createBucketIfNotExist(bucketName);
            }
        }
    }

    private void createBucketIfNotExist(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("[S3 Initialize] '{}' 버킷이 이미 존재합니다.", bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("[S3 Initialize] '{}' 버킷이 자동으로 생성되었습니다! 🎉", bucketName);
                } catch (Exception createException) {
                    log.error("[S3 Initialize Error] 버킷 생성 실패: {}", createException.getMessage());
                }
            }
        }
    }
}