package com.kidmily.algoga_server.stats.presentation;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.stats.application.usecase.ConversionStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.request.PaymentAttemptRequest;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionDailyResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionProductStatsResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionSummaryResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Conversion Stats", description = "예약 전환율 분석 API")
public class ConversionStatsController {

    private final ConversionStatsUseCase conversionStatsUseCase;

    @PostMapping("/api/v1/payment-attempts")
    @Operation(summary = "결제 페이지 진입 기록", description = "프론트에서 결제 페이지 진입 시 호출합니다.")
    public ResponseEntity<ApiResponse<Void>> recordAttempt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentAttemptRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        conversionStatsUseCase.recordAttempt(userId, request.accommodationId());
        return ResponseEntity.ok(ApiResponse.success("PAYMENT_ATTEMPT_RECORDED", "결제 페이지 진입이 기록되었습니다.", null));
    }

    @GetMapping("/api/v1/admin/stats/conversion/summary")
    @Operation(summary = "[어드민] 전환율 요약 조회", description = "결제 페이지 진입 수, 예약 완료 수, 전환율을 조회합니다.")
    public ResponseEntity<ApiResponse<ConversionSummaryResponse>> getSummary(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success("CONVERSION_SUMMARY", "전환율 요약 조회에 성공했습니다.",
                conversionStatsUseCase.getSummary(from, to)));
    }

    @GetMapping("/api/v1/admin/stats/conversion/daily")
    @Operation(summary = "[어드민] 기간별 전환율 조회", description = "일별 전환율 꺾은선 차트 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ConversionDailyResponse>>> getDailyStats(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success("CONVERSION_DAILY", "기간별 전환율 조회에 성공했습니다.",
                conversionStatsUseCase.getDailyStats(from, to)));
    }

    @GetMapping("/api/v1/admin/stats/conversion/products")
    @Operation(summary = "[어드민] 상품별 전환율 조회", description = "상품별 전환율 및 상위/하위 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<ConversionProductStatsResponse>> getProductStats(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success("CONVERSION_PRODUCTS", "상품별 전환율 조회에 성공했습니다.",
                conversionStatsUseCase.getProductStats(from, to)));
    }
}
