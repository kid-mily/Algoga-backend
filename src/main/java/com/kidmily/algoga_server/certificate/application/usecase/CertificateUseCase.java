package com.kidmily.algoga_server.certificate.application.usecase;

import com.kidmily.algoga_server.certificate.application.result.CertificatePdf;

/**
 * 수료증 발급 유스케이스. 프레젠테이션 계층이 구현체(CertificatePdfService) 대신 이 인터페이스에 의존하도록 한다.
 *
 * <p>PDF 바이트와 다운로드 파일명(강의명_학생이름_이수증.pdf)을 CertificatePdf로 함께 반환한다.
 * HTTP 응답 본문(PDF)은 동일하며 다운로드 파일명(Content-Disposition)만 바뀐다.
 */
public interface CertificateUseCase {

    CertificatePdf generateCertificatePdf(Long userId, String userName, Long courseId);
}
