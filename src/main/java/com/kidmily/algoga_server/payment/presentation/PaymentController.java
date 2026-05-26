package com.kidmily.algoga_server.payment.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.application.usecase.PaymentCommandUseCase;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.presentation.api.request.CreatePaymentRequest;
import com.kidmily.algoga_server.payment.presentation.api.request.WebhookRequest;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

    private final PaymentCommandUseCase paymentCommandUseCase;
    private final PaymentQueryUseCase paymentQueryUseCase;

    @PostMapping
    @Operation(summary = "결제 처리", description = "PortOne v2 결제를 처리합니다.")
    @ApiErrorCodeExample(domain = PaymentErrorCode.class,
            value = {"BOOKING_NOT_FOUND", "DUPLICATE_PAYMENT", "INVALID_PAYMENT_AMOUNT", "PORTONE_API_ERROR"})
    public ResponseEntity<ApiResponse<Long>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.bookingId(),
                request.userId(),
                request.paymentType(),
                request.amount(),
                request.usedMileage(),
                request.usedCouponId(),
                request.portonePaymentId()
        );
        Long paymentId = paymentCommandUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("PAYMENT_CREATED", "결제가 완료되었습니다.", paymentId));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 상세 조회", description = "결제 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(domain = PaymentErrorCode.class, value = {"PAYMENT_NOT_FOUND"})
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId
    ) {
        PaymentResponse response = paymentQueryUseCase.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("PAYMENT_FOUND", "결제 조회에 성공했습니다.", response));
    }

    @GetMapping("/{paymentId}/confirmation")
    @Operation(summary = "예약 확인서 PDF", description = "예약 확인서를 PDF로 다운로드합니다.")
    @ApiErrorCodeExample(domain = PaymentErrorCode.class, value = {"PAYMENT_NOT_FOUND", "BOOKING_NOT_FOUND"})
    public ResponseEntity<byte[]> getConfirmation(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId
    ) {
        byte[] pdf = paymentQueryUseCase.getConfirmationPdf(paymentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"confirmation_" + paymentId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/webhook")
    @Operation(summary = "PortOne 웹훅", description = "PortOne 결제 이벤트를 처리합니다.")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody WebhookRequest request
    ) {
        if ("Transaction.Paid".equals(request.type())) {
            paymentCommandUseCase.handleWebhook(request.data().paymentId());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/payments")
    @Operation(summary = "내 결제 내역 조회", description = "유저의 전체 결제 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId
    ) {
        List<PaymentResponse> response = paymentQueryUseCase.getMyPayments(userId);
        return ResponseEntity.ok(ApiResponse.success("MY_PAYMENTS_FOUND", "내 결제 내역 조회에 성공했습니다.", response));
    }
}