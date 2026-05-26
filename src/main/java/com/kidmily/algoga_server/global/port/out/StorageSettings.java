package com.kidmily.algoga_server.global.port.out;

/**
 * 모든 도메인의 스토리지 설정 클래스가 구현해야 하는 공통 인터페이스
 */
public interface StorageSettings {
    String getBucketName();
    String getDirectory();
}