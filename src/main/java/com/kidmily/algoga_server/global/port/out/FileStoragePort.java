package com.kidmily.algoga_server.global.port.out;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 스토리지 통신을 위한 공통 아웃바운드 포트
 * 모든 도메인은 파일을 다룰 때 반드시 버킷명과 폴더명을 명시해야 합니다.
 */
public interface FileStoragePort {

    // 🔥 기본 버킷 오버로딩 제거, 무조건 버킷명(bucketName)을 받도록 강제
    String uploadFile(MultipartFile file, String bucketName, String directory);

    void deleteFile(String bucketName, String fileUrl);
    String uploadFileAsync(java.io.File file, String bucketName, String targetS3Key);
}