package com.kidmily.algoga_server.refund.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.refund.application.command.CreateRefundCommand;
import com.kidmily.algoga_server.refund.application.usecase.RefundCommandUseCase;
import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.exception.RefundErrorCode;
import com.kidmily.algoga_server.refund.presentation.api.request.CreateRefundRequest;
import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Refund", description = "환불 API")
public class RefundController {

    private final RefundCommandUseCase refundCommandUseCase;
    private final RefundQueryUseCase refundQueryUseCase;

    // ── 유저 API ──────────────────────────────────────

    @PostMapping("/api/v1/refund-requests")
    @Operation(summary = "환불 요청", description = "취소된 예약에 대해 환불을 요청합니다.")
    @ApiErrorCodeExample(domain = RefundErrorCode.class,
            value = {"BOOKING_NOT_FOUND", "BOOKING_NOT_CANCELLED", "ALREADY_REFUND_REQUESTED", "PAYMENT_NOT_FOUND"})
    public ResponseEntity<ApiResponse<Long>> createRefund(
            @Valid @RequestBody CreateRefundRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CreateRefundCommand command = new CreateRefundCommand(
                request.bookingId(),
                request.paymentId(),
                userDetails.getUser().getId(),
                request.reason()
        );
        Long refundId = refundCommandUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("REFUND_REQUESTED", "환불 요청이 완료됐습니다.", refundId));
    }

    @GetMapping("/api/v1/refund-requests/me")
    @Operation(summary = "내 환불 요청 목록", description = "본인의 환불 요청 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getMyRefunds(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<RefundResponse> response = refundQueryUseCase.getMyRefunds(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("REFUND_LIST", "환불 목록 조회에 성공했습니다.", response));
    }

    // ── 어드민 API ────────────────────────────────────

    @PostMapping("/api/v1/admin/bookings/{bookingId}/to-refund")
    @Operation(summary = "[어드민] 취소 예약 환불 전환", description = "CS가 취소 요청된 예약을 환불 요청으로 전환합니다.")
    @ApiErrorCodeExample(domain = RefundErrorCode.class,
            value = {"BOOKING_NOT_FOUND", "BOOKING_NOT_CANCELLED", "ALREADY_REFUND_REQUESTED", "PAYMENT_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> convertToRefund(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId
    ) {
        Long refundId = refundCommandUseCase.convertToRefund(bookingId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("REFUND_CONVERTED", "환불 요청으로 전환됐습니다.", refundId));
    }

    @GetMapping("/api/v1/admin/refund-requests")
    @Operation(summary = "[어드민] 환불 요청 목록", description = "전체 환불 요청 목록을 상태/사용자명/예약번호/상품명으로 검색합니다.")
    @PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getAllRefunds(
            @Parameter(description = "환불 상태 필터 (REQUESTED/UNDER_REVIEW/APPROVED/REJECTED/COMPLETED)")
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String bookingNumber,
            @RequestParam(required = false) String productName
    ) {
        List<RefundResponse> response = refundQueryUseCase.getAllRefunds(status, userName, bookingNumber, productName);
        return ResponseEntity.ok(ApiResponse.success("REFUND_LIST", "환불 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/admin/refund-requests/{refundId}")
    @Operation(summary = "[어드민] 환불 단건 조회", description = "환불 요청 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = RefundErrorCode.class, value = {"REFUND_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
            @Parameter(description = "환불 요청 ID", example = "1")
            @PathVariable Long refundId
    ) {
        RefundResponse response = refundQueryUseCase.getRefund(refundId);
        return ResponseEntity.ok(ApiResponse.success("REFUND_DETAIL", "환불 상세 조회에 성공했습니다.", response));
    }

    @GetMapping("/api/v1/admin/refund-requests/excel")
    @Operation(summary = "[어드민] 환불 내역 엑셀 다운로드", description = "환불 내역을 엑셀로 다운로드합니다.")
    @PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<byte[]> getRefundExcel(
            @RequestParam(required = false) RefundStatus status
    ) {
        byte[] excel = refundQueryUseCase.getRefundExcel(status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"refunds.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PutMapping("/api/v1/admin/refund-requests/{refundId}/review")
    @Operation(summary = "[어드민] 환불 검토 요청", description = "CS매니저가 정산매니저에게 환불 검토를 요청합니다. REQUESTED → UNDER_REVIEW")
    @ApiErrorCodeExample(domain = RefundErrorCode.class, value = {"REFUND_NOT_FOUND", "INVALID_REFUND_STATUS"})
    @PreAuthorize("hasAnyAuthority('CS_MANAGER', 'ROLE_CS_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markUnderReview(
            @Parameter(description = "환불 요청 ID", example = "1")
            @PathVariable Long refundId
    ) {
        refundCommandUseCase.markUnderReview(refundId);
        return ResponseEntity.ok(ApiResponse.success("REFUND_UNDER_REVIEW", "환불 검토 요청이 완료됐습니다."));
    }

    @PutMapping("/api/v1/admin/refund-requests/{refundId}/approve")
    @Operation(summary = "[어드민] 환불 승인", description = "환불 요청을 승인합니다. UNDER_REVIEW → APPROVED")
    @ApiErrorCodeExample(domain = RefundErrorCode.class, value = {"REFUND_NOT_FOUND", "INVALID_REFUND_STATUS"})
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @Parameter(description = "환불 요청 ID", example = "1")
            @PathVariable Long refundId
    ) {
        refundCommandUseCase.approve(refundId);
        return ResponseEntity.ok(ApiResponse.success("REFUND_APPROVED", "환불이 승인됐습니다."));
    }

    @PutMapping("/api/v1/admin/refund-requests/{refundId}/reject")
    @Operation(summary = "[어드민] 환불 반려", description = "환불 요청을 반려합니다. REQUESTED → REJECTED")
    @ApiErrorCodeExample(domain = RefundErrorCode.class, value = {"REFUND_NOT_FOUND", "INVALID_REFUND_STATUS"})
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @Parameter(description = "환불 요청 ID", example = "1")
            @PathVariable Long refundId,
            @RequestParam String rejectReason
    ) {
        refundCommandUseCase.reject(refundId, rejectReason);
        return ResponseEntity.ok(ApiResponse.success("REFUND_REJECTED", "환불이 반려됐습니다."));
    }

    @PutMapping("/api/v1/admin/refund-requests/{refundId}/complete")
    @Operation(summary = "[어드민] 환불 완료", description = "환불을 완료 처리합니다. APPROVED → COMPLETED")
    @ApiErrorCodeExample(domain = RefundErrorCode.class, value = {"REFUND_NOT_FOUND", "INVALID_REFUND_STATUS"})
    @PreAuthorize("hasAnyAuthority('SETTLEMENT_MANAGER', 'ROLE_SETTLEMENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> complete(
            @Parameter(description = "환불 요청 ID", example = "1")
            @PathVariable Long refundId
    ) {
        refundCommandUseCase.complete(refundId);
        return ResponseEntity.ok(ApiResponse.success("REFUND_COMPLETED", "환불이 완료됐습니다."));
    }
}