package com.kidmily.algoga_server.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kidmily.algoga_server.global.lock.DistributedLock;
import com.kidmily.algoga_server.payment.application.command.CreateBundlePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.application.usecase.PaymentCommandUseCase;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentPreviewResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentResponse;

import java.util.List;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제 요청의 오케스트레이션(PortOne API 호출 + 트랜잭션 처리 위임)을 담당한다.
 * <p>
 * PortOne API 호출은 DB 트랜잭션 밖에서 먼저 수행하고(STEP 1 — 커넥션 점유 시간 단축),
 * 실제 DB 반영은 {@link PaymentTransactionService} 빈에 위임한다.
 * 같은 클래스 안에서 self-invocation 으로 호출하면 {@code @Transactional}/{@code @CacheEvict}
 * 가 AOP 프록시를 거치지 않아 무시되므로, 반드시 별도 빈을 통해 호출해야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService implements PaymentCommandUseCase {

    private final PortOneClient portOneClient;
    private final PaymentTransactionService paymentTransactionService;
    private final Timer paymentDurationSeconds;
    private final Counter paymentSuccessTotal;
    private final Counter paymentFailedTotal;
    private final Timer portoneApiDurationSeconds;

    @DistributedLock(key = "'payment:' + #command.bookingId()")
    @Override
    public Long handle(CreatePaymentCommand command) {
        log.info("[PaymentCommandService] 결제 요청 - bookingId: {}, type: {}, amount: {}",
                command.bookingId(), command.paymentType(), command.amount());

        return paymentDurationSeconds.record(() -> {
            // PortOne API 호출 시간 측정
            JsonNode portoneResult = portoneApiDurationSeconds.record(
                    () -> portOneClient.getPayment(command.portonePaymentId())
            );
            String portoneStatus = portoneResult.path("status").asText();
            int paidAmount = portoneResult.path("amount").path("total").asInt();
            String paymentMethod = extractPaymentMethod(portoneResult);
            log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}, method: {}", portoneStatus, paidAmount, paymentMethod);

            Long paymentId = paymentTransactionService.savePayment(command, portoneStatus, paidAmount, paymentMethod);

            if ("PAID".equals(portoneStatus)) {
                paymentSuccessTotal.increment();
            } else {
                paymentFailedTotal.increment();
            }

            return paymentId;
        });
    }

    @DistributedLock(key = "'payment:lecture:' + #command.courseId() + ':' + #command.userId()")
    @Override
    public Long handleLecturePayment(CreateLecturePaymentCommand command) {
        log.info("[PaymentCommandService] 강의 단독 결제 요청 - courseId: {}, userId: {}, amount: {}",
                command.courseId(), command.userId(), command.amount());

        // PortOne API 호출을 트랜잭션 밖에서 먼저 수행
        JsonNode portoneResult = portOneClient.getPayment(command.portonePaymentId());
        String portoneStatus = portoneResult.path("status").asText();
        int paidAmount = portoneResult.path("amount").path("total").asInt();
        String paymentMethod = extractPaymentMethod(portoneResult);
        log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}, method: {}", portoneStatus, paidAmount, paymentMethod);

        return paymentTransactionService.saveLecturePayment(command, portoneStatus, paidAmount, paymentMethod);
    }

    /**
     * 통합 결제 사전 검증. PortOne을 부르지 않는 순수 조회라 락·타이머 없이 트랜잭션 빈에 바로 위임한다.
     */
    @Override
    public BundlePaymentPreviewResponse previewBundlePayment(Long bookingId, List<Long> courseIds, Long userId,
                                                             PaymentType paymentType, int usedMileage,
                                                             Long usedCouponId) {
        return paymentTransactionService.previewBundlePayment(
                bookingId, courseIds, userId, paymentType, usedMileage, usedCouponId);
    }

    /**
     * 패키지+강의 통합 결제. PortOne 결제 1건을 검증한 뒤,
     * 예약 결제 1건 + 강의 결제 N건으로 나눠 기록한다.
     * 락은 예약 단위로 잡아 같은 예약에 대한 동시 결제를 막는다.
     */
    @DistributedLock(key = "'payment:' + #command.bookingId()")
    @Override
    public BundlePaymentResponse handleBundlePayment(CreateBundlePaymentCommand command) {
        log.info("[PaymentCommandService] 통합 결제 요청 - bookingId: {}, courseIds: {}, type: {}, amount: {}",
                command.bookingId(), command.courseIds(), command.paymentType(), command.amount());

        return paymentDurationSeconds.record(() -> {
            JsonNode portoneResult = portoneApiDurationSeconds.record(
                    () -> portOneClient.getPayment(command.portonePaymentId())
            );
            String portoneStatus = portoneResult.path("status").asText();
            int paidAmount = portoneResult.path("amount").path("total").asInt();
            String paymentMethod = extractPaymentMethod(portoneResult);
            log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}, method: {}",
                    portoneStatus, paidAmount, paymentMethod);

            BundlePaymentResponse response =
                    paymentTransactionService.saveBundlePayment(command, portoneStatus, paidAmount, paymentMethod);

            if ("PAID".equals(portoneStatus)) {
                paymentSuccessTotal.increment();
            } else {
                paymentFailedTotal.increment();
            }
            return response;
        });
    }

    @Override
    public void handleWebhook(String portonePaymentId) {
        log.info("[PaymentCommandService] 웹훅 수신 - portonePaymentId: {}", portonePaymentId);

        // PortOne API 호출을 트랜잭션 밖에서 먼저 수행
        JsonNode portoneResult = portOneClient.getPayment(portonePaymentId);
        String portoneStatus = portoneResult.path("status").asText();
        String paymentMethod = extractPaymentMethod(portoneResult);

        paymentTransactionService.processWebhook(portonePaymentId, portoneStatus, paymentMethod);
    }

    /**
     * PortOne v2 결제 응답의 {@code method} 노드에서 실제 결제수단을 추출한다.
     * 간편결제는 {@code provider}(TOSSPAY/KAKAOPAY 등), 그 외에는 {@code type}(PaymentMethodCard 등)을 사용.
     * 프론트는 이 코드값을 한글 라벨로 매핑한다. 추출 불가 시 null.
     */
    private String extractPaymentMethod(JsonNode portoneResult) {
        JsonNode method = portoneResult.path("method");
        if (method.isMissingNode() || method.isNull()) {
            return null;
        }
        if (method.hasNonNull("provider")) {
            return method.path("provider").asText();
        }
        if (method.path("easyPay").hasNonNull("provider")) {
            return method.path("easyPay").path("provider").asText();
        }
        if (method.hasNonNull("type")) {
            return method.path("type").asText();
        }
        return null;
    }
}
