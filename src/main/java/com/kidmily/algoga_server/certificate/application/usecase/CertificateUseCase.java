package com.kidmily.algoga_server.certificate.application.usecase;

import com.kidmily.algoga_server.certificate.application.result.CertificatePdf;

public interface CertificateUseCase {

    CertificatePdf generateCertificatePdf(Long userId, String userName, Long courseId);
}
