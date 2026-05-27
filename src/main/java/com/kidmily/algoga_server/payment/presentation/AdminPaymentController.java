package com.kidmily.algoga_server.payment.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Admin Payment", description = "어드민 정산 API")
public class AdminPaymentController {

    private final PaymentQueryUseCase paymentQueryUseCase;

    @GetMapping
    @Operation(summary = "[어드민] 결제 내역 조회", description = "기간별 전체 결제 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAdminPayments(
            @Parameter(description = "시작일 (yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam LocalDate from,
            @Parameter(description = "종료일 (yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam LocalDate to
    ) {
        List<PaymentResponse> response = paymentQueryUseCase.getAdminPayments(from, to);
        return ResponseEntity.ok(ApiResponse.success("ADMIN_PAYMENTS", "결제 내역 조회에 성공했습니다.", response));
    }

    @GetMapping("/excel")
    @Operation(summary = "[어드민] 결제 내역 엑셀 다운로드", description = "기간별 결제 내역을 엑셀로 다운로드합니다.")
    public ResponseEntity<byte[]> getAdminPaymentsExcel(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        byte[] excel = paymentQueryUseCase.getAdminPaymentsExcel(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"payments_" + from + "_" + to + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/stats")
    @Operation(summary = "[어드민] 월별 수익 통계", description = "월별 결제 합계와 건수 통계를 반환합니다.")
    public ResponseEntity<ApiResponse<List<PaymentStatsResponse>>> getAdminPaymentStats() {
        List<PaymentStatsResponse> response = paymentQueryUseCase.getAdminPaymentStats();
        return ResponseEntity.ok(ApiResponse.success("PAYMENT_STATS", "월별 수익 통계 조회에 성공했습니다.", response));
    }
}