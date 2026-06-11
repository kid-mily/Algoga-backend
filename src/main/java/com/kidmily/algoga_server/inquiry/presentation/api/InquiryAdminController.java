// inquiry/presentation/api/InquiryAdminController.java
package com.kidmily.algoga_server.inquiry.presentation.api;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample; // 🌟 추가됨
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminCommandUseCase;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryAdminQueryUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode; // 🌟 추가됨
import com.kidmily.algoga_server.inquiry.presentation.api.request.AnswerInquiryRequest;
import com.kidmily.algoga_server.inquiry.presentation.api.response.InquiryAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry Admin", description = "관리자 전용 1:1 문의 관리 API")
@PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class InquiryAdminController {

    private final InquiryAdminQueryUseCase queryUseCase;
    private final InquiryAdminCommandUseCase commandUseCase;

    @GetMapping
    @Operation(summary = "1:1 문의 목록 페이징 조회", description = "한 페이지에 8개씩, 카테고리(태그)별 필터링이 가능합니다.")
    public ResponseEntity<ApiResponse<PageResponse<InquiryAdminResponse>>> getInquiries(
            @RequestParam(required = false) InquiryCategory category,
            @RequestParam(defaultValue = "0") int page
    ) {
        var pageData = queryUseCase.getAdminInquiries(category, page);
        
        var responseList = pageData.content().stream()
                .map(dto -> new InquiryAdminResponse(
                        dto.inquiryId(), dto.userId(), dto.category(), dto.title(), dto.content(),
                        dto.answer(), dto.status(), dto.createdAt(), dto.answeredAt()
                )).toList();
        
        PageResponse<InquiryAdminResponse> finalResponse = new PageResponse<>(
                responseList, pageData.page(), pageData.size(), pageData.totalElements(),
                pageData.totalPages(), pageData.first(), pageData.last()
        );

        return ResponseEntity.ok(ApiResponse.success("ADMIN_INQUIRIES_LOADED", "문의 목록 조회 성공", finalResponse));
    }

    @PutMapping("/{id}/answer")
    @Operation(summary = "1:1 문의 답변 등록", description = "관리자가 사용자의 문의에 답변을 등록합니다.")
    @ApiErrorCodeExample(domain = InquiryErrorCode.class, value = {
            "INQUIRY_NOT_FOUND", 
            "ALREADY_ANSWERED_INQUIRY"
    }) // 🌟 스웨거 에러 추가
    public ResponseEntity<ApiResponse<Void>> answerInquiry(
            @PathVariable("id") Long id,
            @Valid @RequestBody AnswerInquiryRequest request,
            @CurrentManager Long managerId
    ) {
        commandUseCase.answerInquiry(id, managerId, request.answer());
        return ResponseEntity.ok(ApiResponse.success("INQUIRY_ANSWERED", "문의에 대한 답변이 등록되었습니다.", null));
    }
}