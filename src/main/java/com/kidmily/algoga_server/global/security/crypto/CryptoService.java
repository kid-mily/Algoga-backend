package com.kidmily.algoga_server.global.security.crypto;

/**
 * 민감 정보의 양방향 암호화/복호화 계약.
 * 저장 시 암호화, 조회 시 복호화가 필요한 도메인(예: 여권)이 재사용한다.
 */
public interface CryptoService {

    /**
     * 평문을 암호화한다. null 입력은 null을 반환한다.
     */
    String encrypt(String plainText);

    /**
     * 암호문을 복호화한다. null 입력은 null을 반환한다.
     */
    String decrypt(String encrypted);
}
