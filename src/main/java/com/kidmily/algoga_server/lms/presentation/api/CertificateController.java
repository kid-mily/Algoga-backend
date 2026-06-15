package com.kidmily.algoga_server.lms.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.lms.application.service.CertificatePdfService;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.support.CurrentUserIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Tag(name = "수료증", description = "강의 수료증 PDF 발급 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificatePdfService certificatePdfService;

    @Operation(
            summary = "수료증 PDF 발급",
            description = """
                    강의 이수 완료 내역이 있는 사용자에게 수료증 PDF를 발급합니다.
                    PDF에는 사용자명, 강의명, 수료일, 수료 코드가 포함됩니다.
                    """
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "COURSE_COMPLETION_NOT_FOUND"
    })
    @GetMapping
    public ResponseEntity<byte[]> downloadCertificate(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @AuthenticationPrincipal Object userDetails
    ) {
        Long currentUserId = CurrentUserIdResolver.resolveNullable(userDetails);
        String userName = CurrentUserIdResolver.resolveNameNullable(userDetails);

        byte[] pdfBytes = certificatePdfService.generateCertificatePdf(
                currentUserId,
                userName,
                courseId
        );

        String filename = "certificate-course-" + courseId + ".pdf";

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(pdfBytes);
    }
}
