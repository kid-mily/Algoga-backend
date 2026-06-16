package com.kidmily.algoga_server.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // ── Booking ──────────────────────────────────────────────────────────────

    /** 예약 생성 요청 횟수 */
    @Bean
    public Counter bookingCreatedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_booking_created_total")
                .description("총 예약 생성 횟수")
                .register(registry);
    }

    /** 예약 취소 요청 횟수 */
    @Bean
    public Counter bookingCanceledTotal(MeterRegistry registry) {
        return Counter.builder("algoga_booking_canceled_total")
                .description("총 예약 취소 횟수")
                .register(registry);
    }

    // ── Payment ──────────────────────────────────────────────────────────────

    /** 결제 처리 소요 시간 */
    @Bean
    public Timer paymentDurationSeconds(MeterRegistry registry) {
        return Timer.builder("algoga_payment_duration_seconds")
                .description("결제 처리 소요 시간")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    /** 결제 성공 횟수 */
    @Bean
    public Counter paymentSuccessTotal(MeterRegistry registry) {
        return Counter.builder("algoga_payment_total")
                .description("결제 처리 횟수")
                .tag("result", "success")
                .register(registry);
    }

    /** 결제 실패 횟수 */
    @Bean
    public Counter paymentFailedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_payment_total")
                .description("결제 처리 횟수")
                .tag("result", "failed")
                .register(registry);
    }

    /** PortOne API 호출 소요 시간 */
    @Bean
    public Timer portoneApiDurationSeconds(MeterRegistry registry) {
        return Timer.builder("algoga_portone_api_duration_seconds")
                .description("PortOne API 호출 소요 시간 (p99 모니터링)")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    // ── Refund ───────────────────────────────────────────────────────────────

    /** 환불 요청 횟수 */
    @Bean
    public Counter refundRequestedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_refund_requested_total")
                .description("총 환불 요청 횟수")
                .register(registry);
    }

    /** 환불 승인 횟수 */
    @Bean
    public Counter refundApprovedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_refund_total")
                .description("환불 처리 횟수")
                .tag("result", "approved")
                .register(registry);
    }

    /** 환불 반려 횟수 */
    @Bean
    public Counter refundRejectedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_refund_total")
                .description("환불 처리 횟수")
                .tag("result", "rejected")
                .register(registry);
    }

    // ── API 에러 ─────────────────────────────────────────────────────────────
    // algoga_api_errors_total{reason=...} 는 CommonExceptionAdvice.recordApiError()에서
    // MeterRegistry.counter()로 동적으로 등록/증가시킴 (access_denied / business / validation /
    // bad_request / method_not_allowed / not_found / server_error)
}
