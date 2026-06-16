package com.kidmily.algoga_server.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.application.usecase.PaymentCommandUseCase;
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
            log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}", portoneStatus, paidAmount);

            Long paymentId = paymentTransactionService.savePayment(command, portoneStatus, paidAmount);

            if ("PAID".equals(portoneStatus)) {
                paymentSuccessTotal.increment();
            } else {
                paymentFailedTotal.increment();
            }

            return paymentId;
        });
    }

    @Override
    public Long handleLecturePayment(CreateLecturePaymentCommand command) {
        log.info("[PaymentCommandService] 강의 단독 결제 요청 - courseId: {}, userId: {}, amount: {}",
                command.courseId(), command.userId(), command.amount());

        // PortOne API 호출을 트랜잭션 밖에서 먼저 수행
        JsonNode portoneResult = portOneClient.getPayment(command.portonePaymentId());
        String portoneStatus = portoneResult.path("status").asText();
        int paidAmount = portoneResult.path("amount").path("total").asInt();
        log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}", portoneStatus, paidAmount);

        return paymentTransactionService.saveLecturePayment(command, portoneStatus, paidAmount);
    }

    @Override
    public void handleWebhook(String portonePaymentId) {
        log.info("[PaymentCommandService] 웹훅 수신 - portonePaymentId: {}", portonePaymentId);

        // PortOne API 호출을 트랜잭션 밖에서 먼저 수행
        JsonNode portoneResult = portOneClient.getPayment(portonePaymentId);
        String portoneStatus = portoneResult.path("status").asText();

        paymentTransactionService.processWebhook(portonePaymentId, portoneStatus);
    }
}
