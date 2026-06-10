// inquiry/presentation/api/InquiryController.java
package com.kidmily.algoga_server.inquiry.presentation.api;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryCommandUseCase;
import com.kidmily.algoga_server.inquiry.presentation.api.request.CreateInquiryRequest;
// 🌟 명세해주신 경로 적용
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import com.kidmily.algoga_server.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry", description = "1:1 고객 문의 도메인 API")
public class InquiryController {

    private final InquiryCommandUseCase inquiryCommandUseCase;

    @PostMapping
    @Operation(summary = "1:1 수동 문의 작성", description = "관리자에게 직접 문의 내용을 남깁니다.")
    public ResponseEntity<ApiResponse<Void>> createInquiry(
            @Valid @RequestBody CreateInquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails // 🌟 Security 주입
    ) {
        Long userId = userDetails.getUser().getId(); // 🌟 User 엔티티에서 PK 추출

        inquiryCommandUseCase.createInquiry(userId, request.question());

        return ResponseEntity.ok(ApiResponse.success("INQUIRY_CREATED", "성공적으로 1:1 문의가 접수되었습니다.", null));
    }
}