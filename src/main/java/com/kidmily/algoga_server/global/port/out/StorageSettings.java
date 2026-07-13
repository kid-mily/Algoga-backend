package com.kidmily.algoga_server.global.port.out;

/**
 * 각 도메인의 스토리지 설정이 구현하는 공통 인터페이스.
 *
 * <p>버킷은 전역 단일 버킷({@code S3Settings})으로 통일되어 더 이상 도메인별 버킷을 두지 않는다.
 * 도메인 구분은 object key의 directory(prefix)로 표현한다.
 */
public interface StorageSettings {
    String getDirectory();
}
