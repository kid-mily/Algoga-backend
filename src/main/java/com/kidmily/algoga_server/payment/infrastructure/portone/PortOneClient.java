package com.kidmily.algoga_server.payment.infrastructure.portone;

import com.fasterxml.jackson.databind.JsonNode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@Component
public class PortOneClient {

    private static final String PORTONE_CB = "portone";

    private final RestClient restClient;
    private final PortOneProperties properties;

    public PortOneClient(PortOneProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + properties.getApiSecret())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @CircuitBreaker(name = PORTONE_CB, fallbackMethod = "getPaymentFallback")
    public JsonNode getPayment(String portonePaymentId) {
        log.info("[PortOneClient] 결제 조회 요청 - portonePaymentId: {}", portonePaymentId);
        try {
            return restClient.get()
                    .uri("/payments/{paymentId}", portonePaymentId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            // PortOne이 4xx/5xx를 준 경우 — 상태코드와 응답 본문이 원인 판별의 핵심이라 함께 남긴다
            log.warn("[PortOneClient] PortOne 조회 API 응답 오류 - portonePaymentId: {}, status: {}, body: {}",
                    portonePaymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
        } catch (Exception e) {
            // 타임아웃·연결 실패 등 — 스택트레이스까지 남긴다
            log.warn("[PortOneClient] PortOne 조회 API 호출 실패 - portonePaymentId: {}", portonePaymentId, e);
            throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
        }
    }

    @CircuitBreaker(name = PORTONE_CB, fallbackMethod = "cancelPaymentFallback")
    public void cancelPayment(String portonePaymentId, int amount, String reason) {
        log.info("[PortOneClient] 결제 취소 요청 - portonePaymentId: {}", portonePaymentId);
        try {
            restClient.post()
                    .uri("/payments/{paymentId}/cancel", portonePaymentId)
                    .body(Map.of("reason", reason, "amount", amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // 예: 존재하지 않는 결제건 취소 시도(테스트 데이터), 인증 실패 — 둘 다 PAY_005로 올라가므로 로그로만 구분된다
            log.warn("[PortOneClient] PortOne 취소 API 응답 오류 - portonePaymentId: {}, status: {}, body: {}",
                    portonePaymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
        } catch (Exception e) {
            log.warn("[PortOneClient] PortOne 취소 API 호출 실패 - portonePaymentId: {}", portonePaymentId, e);
            throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
        }
    }

    // 서킷브레이커 OPEN 상태일 때 호출되는 fallback
    private JsonNode getPaymentFallback(String portonePaymentId, CallNotPermittedException e) {
        log.error("[PortOneClient] 서킷브레이커 OPEN - 결제 조회 차단됨 portonePaymentId: {}", portonePaymentId);
        throw new BusinessException(PaymentErrorCode.PORTONE_CIRCUIT_OPEN);
    }

    private void cancelPaymentFallback(String portonePaymentId, int amount, String reason, CallNotPermittedException e) {
        log.error("[PortOneClient] 서킷브레이커 OPEN - 결제 취소 차단됨 portonePaymentId: {}", portonePaymentId);
        throw new BusinessException(PaymentErrorCode.PORTONE_CIRCUIT_OPEN);
    }
}