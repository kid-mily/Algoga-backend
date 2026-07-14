package com.kidmily.algoga_server.certificate.application.usecase;

/**
 * 수료증 발급 유스케이스. 프레젠테이션 계층이 구현체(CertificatePdfService) 대신 이 인터페이스에 의존하도록 한다.
 *
 * <p>메서드 시그니처는 기존 CertificatePdfService의 공개 메서드와 동일하여 동작·API 계약 변화가 없다.
 */
public interface CertificateUseCase {

    byte[] generateCertificatePdf(Long userId, String userName, Long courseId);
}
