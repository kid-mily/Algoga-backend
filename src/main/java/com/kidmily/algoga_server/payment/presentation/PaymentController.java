package com.kidmily.algoga_server.payment.presentation;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.payment.application.command.CreateBundlePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.application.usecase.PaymentCommandUseCase;
import com.kidmily.algoga_server.payment.application.usecase.PaymentQueryUseCase;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.presentation.api.request.CreateBundlePaymentRequest;
import com.kidmily.algoga_server.payment.presentation.api.request.CreateLecturePaymentRequest;
import com.kidmily.algoga_server.payment.presentation.api.request.CreatePaymentRequest;
import com.kidmily.algoga_server.payment.presentation.api.request.WebhookRequest;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentPreviewResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.PaymentResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.bookingId(),
                userId,
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

    @PostMapping("/lecture")
    @Operation(summary = "강의 단독 결제", description = "패키지 없이 강의만 단독으로 결제합니다. 쿠폰 및 마일리지 적용 가능합니다.")
    @ApiErrorCodeExample(domain = PaymentErrorCode.class,
            value = {"COURSE_NOT_FOUND", "DUPLICATE_PAYMENT", "INVALID_PAYMENT_AMOUNT", "PORTONE_API_ERROR"})
    public ResponseEntity<ApiResponse<Long>> createLecturePayment(
            @Valid @RequestBody CreateLecturePaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CreateLecturePaymentCommand command = new CreateLecturePaymentCommand(
                request.courseId(),
                userId,
                request.amount(),
                request.usedMileage(),
                request.usedCouponId(),
                request.portonePaymentId()
        );
        Long paymentId = paymentCommandUseCase.handleLecturePayment(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("LECTURE_PAYMENT_CREATED", "강의 결제가 완료되었습니다.", paymentId));
    }

    @PostMapping("/bundle")
    @Operation(summary = "패키지+강의 통합 결제",
            description = "PortOne 결제 **1회**로 패키지(예약)와 강의를 함께 결제합니다.\n\n"
                    + "**금액 규칙**\n"
                    + "- 총액 = 패키지분 + 강의 정가 합계 − 쿠폰할인 − 마일리지\n"
                    + "- 패키지분: `paymentType=DEPOSIT`이면 예약금(30%), `FULL`이면 전액\n"
                    + "- 강의는 분할 개념이 없어 **항상 정가 전액**, 쿠폰·마일리지는 **패키지분에만** 적용\n\n"
                    + "**주의**\n"
                    + "- `courseIds`에는 이미 결제한 강의를 넣으면 안 됩니다(`isPaid=true` 제외). 넣으면 DUPLICATE_PAYMENT.\n"
                    + "- 완강 후 예약(installmentAllowed=false)은 `FULL`만 가능합니다.\n"
                    + "- 결제 성공 시 강의 수강권이 자동 생성됩니다.")
    @ApiErrorCodeExample(domain = PaymentErrorCode.class,
            value = {"BOOKING_NOT_FOUND", "COURSE_NOT_FOUND", "DUPLICATE_PAYMENT",
                    "INVALID_PAYMENT_AMOUNT", "INVALID_PAYMENT_TYPE", "INSTALLMENT_NOT_ALLOWED", "PORTONE_API_ERROR"})
    public ResponseEntity<ApiResponse<BundlePaymentResponse>> createBundlePayment(
            @Valid @RequestBody CreateBundlePaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CreateBundlePaymentCommand command = new CreateBundlePaymentCommand(
                request.bookingId(),
                request.courseIds(),
                userId,
                request.paymentType(),
                request.amount(),
                request.usedMileage(),
                request.usedCouponId(),
                request.portonePaymentId()
        );
        BundlePaymentResponse response = paymentCommandUseCase.handleBundlePayment(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("BUNDLE_PAYMENT_CREATED", "패키지+강의 결제가 완료되었습니다.", response));
    }

    @GetMapping("/bundle/preview")
    @Operation(summary = "패키지+강의 통합 결제 사전 검증",
            description = "PortOne 결제창을 **띄우기 전에** 호출해서 결제 가능 여부와 청구 예정액을 확인합니다. "
                    + "DB를 바꾸지 않습니다.\n\n"
                    + "**왜 필요한가**\n"
                    + "통합 결제 본 API(`POST /bundle`)는 PortOne 결제가 **끝난 뒤** 검증합니다. "
                    + "이미 산 강의가 섞여 있으면 **돈이 빠져나간 뒤에** DUPLICATE_PAYMENT로 거부되어, "
                    + "청구는 됐는데 서버에 기록이 없는 상태가 됩니다. 이 API로 먼저 걸러야 합니다.\n\n"
                    + "**사용법**\n"
                    + "1. `payable=false`면 결제창을 띄우지 말고 `blockMessage`를 노출\n"
                    + "2. `blockReason=DUPLICATE_PAYMENT`이고 `alreadyPaidCourseIds`가 있으면, "
                    + "그 강의를 `courseIds`에서 빼고 다시 호출\n"
                    + "3. `payable=true`면 `expectedTotal` 금액으로 PortOne 결제 후 `POST /bundle` 호출")
    public ResponseEntity<ApiResponse<BundlePaymentPreviewResponse>> previewBundlePayment(
            @RequestParam Long bookingId,
            @Parameter(description = "함께 결제할 강의 ID 목록. 없으면 생략")
            @RequestParam(required = false) List<Long> courseIds,
            @Parameter(description = "DEPOSIT(예약금 30%) 또는 FULL(전액)")
            @RequestParam PaymentType paymentType,
            @RequestParam(defaultValue = "0") int usedMileage,
            @RequestParam(required = false) Long usedCouponId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BundlePaymentPreviewResponse response = paymentCommandUseCase.previewBundlePayment(
                bookingId,
                courseIds == null ? List.of() : courseIds,
                userDetails.getUser().getId(),
                paymentType,
                usedMileage,
                usedCouponId
        );
        return ResponseEntity.ok(
                ApiResponse.success("BUNDLE_PAYMENT_PREVIEW", "통합 결제 사전 검증 결과입니다.", response));
    }

    @GetMapping("/calculate/lecture")
    @Operation(summary = "강의 결제 금액 계산", description = "쿠폰 및 마일리지 적용 후 최종 결제 금액을 반환합니다.")
    public ResponseEntity<ApiResponse<Integer>> calculateLectureAmount(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "0") int usedMileage,
            @RequestParam(required = false) Long usedCouponId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int finalAmount = paymentQueryUseCase.calculateLectureAmount(
                courseId, usedMileage, usedCouponId, userDetails.getUser().getId()
        );
        return ResponseEntity.ok(ApiResponse.success("LECTURE_AMOUNT_CALCULATED", "결제 금액 계산에 성공했습니다.", finalAmount));
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

    @PostMapping("/webhook")
    @Operation(summary = "PortOne 웹훅", description = "PortOne 결제 이벤트를 처리합니다.")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody WebhookRequest request
    ) {
        if ("Transaction.Paid".equals(request.type())) {
            paymentCommandUseCase.handleWebhook(request.data().paymentId());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 결제 내역 조회", description = "로그인한 유저의 전체 결제 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<PaymentResponse> response = paymentQueryUseCase.getMyPayments(userId);
        return ResponseEntity.ok(ApiResponse.success("MY_PAYMENTS_FOUND", "내 결제 내역 조회에 성공했습니다.", response));
    }
}