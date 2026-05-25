package com.kidmily.algoga_server.payment.infrastructure.portone;

import com.fasterxml.jackson.databind.JsonNode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class PortOneClient {

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

    // 결제 단건 조회 - 검증용
    public JsonNode getPayment(String portonePaymentId) {
        log.info("[PortOneClient] 결제 조회 요청 - portonePaymentId: {}", portonePaymentId);
        try {
            return restClient.get()
                    .uri("/payments/{paymentId}", portonePaymentId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.warn("[PortOneClient] PortOne API 호출 실패 - portonePaymentId: {}", portonePaymentId);
            throw new BusinessException(PaymentErrorCode.PORTONE_API_ERROR);
        }
    }
}