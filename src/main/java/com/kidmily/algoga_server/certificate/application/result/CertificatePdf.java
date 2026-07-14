package com.kidmily.algoga_server.certificate.application.result;

/**
 * 수료증 PDF 발급 결과. PDF 바이트와 다운로드용 파일명(강의명_학생이름_이수증.pdf)을 함께 담는다.
 */
public record CertificatePdf(
        byte[] content,
        String fileName
) {
}
