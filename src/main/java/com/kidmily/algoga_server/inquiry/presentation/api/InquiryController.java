// inquiry/presentation/api/InquiryController.java
package com.kidmily.algoga_server.inquiry.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample; // 🌟 추가됨
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.inquiry.application.usecase.InquiryCommandUseCase;
import com.kidmily.algoga_server.inquiry.domain.model.InquiryCategory;
import com.kidmily.algoga_server.inquiry.exception.InquiryErrorCode; // 🌟 추가됨
import com.kidmily.algoga_server.inquiry.presentation.api.request.CreateInquiryRequest;
import com.kidmily.algoga_server.inquiry.presentation.api.response.InquiryCategoryResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry", description = "1:1 고객 문의 도메인 API")
public class InquiryController {

    private final InquiryCommandUseCase inquiryCommandUseCase;

    @GetMapping("/categories")
    @Operation(summary = "문의 카테고리(태그) 목록 조회", description = "1:1 문의 작성 시 선택할 수 있는 카테고리 태그 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<InquiryCategoryResponse>>> getCategories() {
        List<InquiryCategoryResponse> categoryList = Arrays.stream(InquiryCategory.values())
                .map(category -> new InquiryCategoryResponse(category.name(), category.getDescription()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("INQUIRY_CATEGORIES_LOADED", "문의 카테고리 목록 조회 성공", categoryList));
    }

    @PostMapping
    @Operation(summary = "1:1 수동 문의 작성", description = "관리자에게 태그, 제목, 내용을 포함하여 직접 문의를 남깁니다.")
    @ApiErrorCodeExample(domain = InquiryErrorCode.class, value = {
            "INVALID_USER_ID", 
            "INVALID_INQUIRY_TITLE", 
            "INVALID_INQUIRY_CONTENT"
    }) // 🌟 스웨거 에러 추가
    public ResponseEntity<ApiResponse<Void>> createInquiry(
            @Valid @RequestBody CreateInquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        inquiryCommandUseCase.createInquiry(userId, request.category(), request.title(), request.content());
        return ResponseEntity.ok(ApiResponse.success("INQUIRY_CREATED", "성공적으로 1:1 문의가 접수되었습니다.", null));
    }

    @PatchMapping("/{id}/answer/read")
    @Operation(summary = "문의 답변 확인 처리", description = "챗봇 창에서 사용자가 해당 문의의 답변을 확인했을 때 호출합니다. '답변 완료' 뱃지를 해제합니다.")
    @ApiErrorCodeExample(domain = InquiryErrorCode.class, value = {"INQUIRY_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Void>> markAnswerRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        inquiryCommandUseCase.markAnswerRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success("INQUIRY_ANSWER_READ", "문의 답변을 확인 처리했습니다.", null));
    }
}