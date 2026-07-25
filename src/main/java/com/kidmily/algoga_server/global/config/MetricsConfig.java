package com.kidmily.algoga_server.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    // ── User ─────────────────────────────────────────────────────────────────

    /** 일반(LOCAL) 회원가입 완료 횟수 */
    @Bean
    public Counter userSignupLocalTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_signup_total")
                .description("회원가입 완료 횟수")
                .tag("socialType", "LOCAL")
                .register(registry);
    }

    /** 구글 소셜 회원가입 완료 횟수 */
    @Bean
    public Counter userSignupGoogleTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_signup_total")
                .description("회원가입 완료 횟수")
                .tag("socialType", "GOOGLE")
                .register(registry);
    }

    /** 카카오 소셜 회원가입 완료 횟수 */
    @Bean
    public Counter userSignupKakaoTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_signup_total")
                .description("회원가입 완료 횟수")
                .tag("socialType", "KAKAO")
                .register(registry);
    }

    /** 로그인 성공 횟수 */
    @Bean
    public Counter userLoginSuccessTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_login_total")
                .description("로그인 시도 횟수")
                .tag("result", "success")
                .register(registry);
    }

    /** 로그인 실패 횟수 (탈퇴/잠금/블랙리스트/비밀번호 오류 등 전부 포함) */
    @Bean
    public Counter userLoginFailedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_login_total")
                .description("로그인 시도 횟수")
                .tag("result", "failed")
                .register(registry);
    }

    /** 회원 탈퇴 처리 횟수 */
    @Bean
    public Counter userWithdrawTotal(MeterRegistry registry) {
        return Counter.builder("algoga_user_withdraw_total")
                .description("총 회원 탈퇴 처리 횟수")
                .register(registry);
    }

    // ── Friend ───────────────────────────────────────────────────────────────

    /** 친구 요청 발송 횟수 */
    @Bean
    public Counter friendRequestSentTotal(MeterRegistry registry) {
        return Counter.builder("algoga_friend_request_total")
                .description("친구 요청 처리 횟수")
                .tag("result", "sent")
                .register(registry);
    }

    /** 친구 요청 수락 횟수 */
    @Bean
    public Counter friendRequestAcceptedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_friend_request_total")
                .description("친구 요청 처리 횟수")
                .tag("result", "accepted")
                .register(registry);
    }

    /** 친구 요청 거절 횟수 */
    @Bean
    public Counter friendRequestRejectedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_friend_request_total")
                .description("친구 요청 처리 횟수")
                .tag("result", "rejected")
                .register(registry);
    }

    /** 유저 차단 처리 횟수 */
    @Bean
    public Counter friendBlockedTotal(MeterRegistry registry) {
        return Counter.builder("algoga_friend_blocked_total")
                .description("총 유저 차단 처리 횟수")
                .register(registry);
    }

    /**
     * 실시간 온라인 유저 수. Gauge는 증감 메서드가 따로 없어서, 실제 값을 들고 있는
     * AtomicInteger를 별도 빈으로 노출해 PresenceEventListener가 접속/해제 시 직접 증감시키고,
     * Gauge는 그 값을 읽기만 한다.
     */
    @Bean
    public AtomicInteger onlineUserCountValue() {
        return new AtomicInteger(0);
    }

    @Bean
    public Gauge onlineUserCount(MeterRegistry registry, AtomicInteger onlineUserCountValue) {
        return Gauge.builder("algoga_friend_online_users", onlineUserCountValue, AtomicInteger::get)
                .description("현재 온라인(WebSocket 연결 중)인 유저 수")
                .register(registry);
    }

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

    // ── Payment Funnel ───────────────────────────────────────────────────────

    /** 결제 페이지 진입 횟수 */
    @Bean
    public Counter paymentAttemptTotal(MeterRegistry registry) {
        return Counter.builder("algoga_payment_attempt_total")
                .description("결제 페이지 진입 횟수")
                .register(registry);
    }

    // ── Chat / WebSocket ──────────────────────────────────────────────────────

    /** WebSocket 연결 수립 횟수 (rate로 접속 유입 추이 관찰) */
    @Bean
    public Counter chatWsConnectTotal(MeterRegistry registry) {
        return Counter.builder("algoga_chat_ws_connection_total")
                .description("WebSocket 연결 이벤트 횟수")
                .tag("event", "connect")
                .register(registry);
    }

    /** WebSocket 연결 해제 횟수 (스파이크 = 배포/서버 튕김 신호) */
    @Bean
    public Counter chatWsDisconnectTotal(MeterRegistry registry) {
        return Counter.builder("algoga_chat_ws_connection_total")
                .description("WebSocket 연결 이벤트 횟수")
                .tag("event", "disconnect")
                .register(registry);
    }

    // ── API 에러 ─────────────────────────────────────────────────────────────
    // algoga_api_errors_total{reason=...} 는 CommonExceptionAdvice.recordApiError()에서
    // MeterRegistry.counter()로 동적으로 등록/증가시킴 (access_denied / business / validation /
    // bad_request / method_not_allowed / not_found / server_error)
}
