package com.kidmily.algoga_server.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * PaymentCommandService.extractPaymentMethod() 검증.
 * PortOne 응답(method 노드)에서 실제 결제수단을 뽑아 트랜잭션 서비스로 넘기는지를,
 * PortOne 호출을 Mock 으로 대체해(실제 호출 없이) handleLecturePayment 경로로 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PortOneClient portOneClient;
    @Mock private PaymentTransactionService paymentTransactionService;
    @Mock private Timer paymentDurationSeconds;
    @Mock private Counter paymentSuccessTotal;
    @Mock private Counter paymentFailedTotal;
    @Mock private Timer portoneApiDurationSeconds;

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Test
    @DisplayName("간편결제면 method.provider(TOSSPAY)가 결제수단으로 전달된다")
    void 간편결제_provider_추출() throws Exception {
        JsonNode fake = objectMapper.readTree(
                "{\"status\":\"PAID\",\"amount\":{\"total\":100000},\"method\":{\"provider\":\"TOSSPAY\"}}");
        when(portOneClient.getPayment("pay-1")).thenReturn(fake);
        when(paymentTransactionService.saveLecturePayment(any(), anyString(), anyInt(), anyString()))
                .thenReturn(1L);

        CreateLecturePaymentCommand command =
                new CreateLecturePaymentCommand(10L, 2L, 100000, 0, null, "pay-1");

        Long id = paymentCommandService.handleLecturePayment(command);

        assertEquals(1L, id);
        verify(paymentTransactionService)
                .saveLecturePayment(any(), eq("PAID"), eq(100000), eq("TOSSPAY"));
    }

    @Test
    @DisplayName("카드 등은 method.type(PaymentMethodCard)이 결제수단으로 전달된다")
    void 카드_type_추출() throws Exception {
        JsonNode fake = objectMapper.readTree(
                "{\"status\":\"PAID\",\"amount\":{\"total\":50000},\"method\":{\"type\":\"PaymentMethodCard\"}}");
        when(portOneClient.getPayment("pay-2")).thenReturn(fake);
        when(paymentTransactionService.saveLecturePayment(any(), anyString(), anyInt(), anyString()))
                .thenReturn(2L);

        CreateLecturePaymentCommand command =
                new CreateLecturePaymentCommand(11L, 2L, 50000, 0, null, "pay-2");

        paymentCommandService.handleLecturePayment(command);

        verify(paymentTransactionService)
                .saveLecturePayment(any(), eq("PAID"), eq(50000), eq("PaymentMethodCard"));
    }

    @Test
    @DisplayName("method 정보가 없으면 결제수단은 null 로 전달된다")
    void method_없으면_null() throws Exception {
        JsonNode fake = objectMapper.readTree(
                "{\"status\":\"FAILED\",\"amount\":{\"total\":0}}");
        when(portOneClient.getPayment("pay-3")).thenReturn(fake);
        when(paymentTransactionService.saveLecturePayment(any(), anyString(), anyInt(), isNull()))
                .thenReturn(3L);

        CreateLecturePaymentCommand command =
                new CreateLecturePaymentCommand(12L, 2L, 0, 0, null, "pay-3");

        paymentCommandService.handleLecturePayment(command);

        verify(paymentTransactionService)
                .saveLecturePayment(any(), eq("FAILED"), eq(0), isNull());
    }
}
