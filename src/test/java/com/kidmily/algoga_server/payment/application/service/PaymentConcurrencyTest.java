package com.kidmily.algoga_server.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * A2 블로그("분산락이 실제로 중복 결제를 막는가") 근거자료.
 * <p>
 * 같은 예약(bookingId)에 대해 결제 요청 N개를 동시에 쏴서,
 * (1) 락이 없는 {@link PaymentTransactionService#savePayment}를 직접 호출했을 때와
 * (2) {@link PaymentCommandService#handle}(= @DistributedLock이 걸린 프록시 빈)을 호출했을 때
 * 결과가 어떻게 다른지 실측한다. PortOne 호출만 Mock, DB(MySQL)·Redis(Redisson)는 로컬 실제 인스턴스 사용.
 */
@SpringBootTest
class PaymentConcurrencyTest {

    private static final Long TEST_ACCOMMODATION_ID = 1L;
    private static final Long TEST_USER_ID = 5L; // testuser
    private static final int THREAD_COUNT = 10;

    @Autowired private PaymentCommandService paymentCommandService;
    @Autowired private PaymentTransactionService paymentTransactionService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean private PortOneClient portOneClient;

    private final List<Long> createdBookingIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (Long bookingId : createdBookingIds) {
            jdbcTemplate.update("DELETE FROM payments WHERE booking_id = ?", bookingId);
            jdbcTemplate.update("DELETE FROM bookings WHERE booking_id = ?", bookingId);
        }
        createdBookingIds.clear();
    }

    @Test
    @DisplayName("락 없이 같은 예약에 동시 결제 시도 -> DB 유니크 제약이 막긴 하지만 날것의 예외로 실패한다")
    void 락없이_동시결제() throws Exception {
        Long bookingId = createTestBooking();
        when(portOneClient.getPayment(anyString())).thenReturn(fakePaidResponse(300_000));

        CreatePaymentCommand command =
                new CreatePaymentCommand(bookingId, TEST_USER_ID, PaymentType.DEPOSIT, 300_000, 0, null, "portone-nolock");

        ConcurrencyResult result = runConcurrently(THREAD_COUNT,
                () -> paymentTransactionService.savePayment(command, "PAID", 300_000, "CARD"));

        long paymentRows = countPayments(bookingId);

        System.out.println("[락 없음] 성공 " + result.successCount() + "건");
        result.failureTypes().forEach(f -> System.out.println("[락 없음] 실패: " + f));
        System.out.println("[락 없음] 최종 payments 행 수: " + paymentRows);

        assertEquals(1, paymentRows, "동시 요청이 몰려도 DB 유니크 제약 덕분에 결제 행은 결국 1건만 남아야 한다");
        assertEquals(1, result.successCount(), "성공은 1건만 있어야 한다");
    }

    @Test
    @DisplayName("분산락 적용 상태로 동시 결제 시도 -> 1건만 성공, 나머지는 애플리케이션 레벨 예외(DUPLICATE_PAYMENT 등)로 깔끔하게 거부된다")
    void 락있이_동시결제() throws Exception {
        Long bookingId = createTestBooking();
        when(portOneClient.getPayment(anyString())).thenReturn(fakePaidResponse(300_000));

        CreatePaymentCommand command =
                new CreatePaymentCommand(bookingId, TEST_USER_ID, PaymentType.DEPOSIT, 300_000, 0, null, "portone-lock");

        ConcurrencyResult result = runConcurrently(THREAD_COUNT,
                () -> paymentCommandService.handle(command));

        long paymentRows = countPayments(bookingId);

        System.out.println("[락 있음] 성공 " + result.successCount() + "건");
        result.failureTypes().forEach(f -> System.out.println("[락 있음] 실패: " + f));
        System.out.println("[락 있음] 최종 payments 행 수: " + paymentRows);

        assertEquals(1, paymentRows, "결제 행은 1건만 있어야 한다");
        assertEquals(1, result.successCount(), "성공은 1건만 있어야 한다");
        assertTrue(result.failureTypes().stream().allMatch(f -> f.startsWith("BusinessException")),
                "나머지는 전부 날것의 DB 예외가 아니라 BusinessException(PAY_004 또는 GLOBAL_006)이어야 한다");
    }

    private Long createTestBooking() {
        Booking booking = Booking.create(
                TEST_ACCOMMODATION_ID, TEST_USER_ID,
                1_000_000, 300_000, 700_000,
                "BK-TEST-" + System.nanoTime(),
                null, null, null,
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(31),
                1, true, null
        );
        Long id = bookingRepository.save(booking).getId();
        createdBookingIds.add(id);
        return id;
    }

    private JsonNode fakePaidResponse(int amount) throws Exception {
        return new ObjectMapper().readTree(
                "{\"status\":\"PAID\",\"amount\":{\"total\":" + amount + "},\"method\":{\"type\":\"PaymentMethodCard\"}}");
    }

    private long countPayments(Long bookingId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE booking_id = ?", Long.class, bookingId);
        return count == null ? 0 : count;
    }

    private ConcurrencyResult runConcurrently(int threadCount, ThrowingRunnable task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        List<String> failureTypes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    String label = (t instanceof BusinessException be)
                            ? "BusinessException[" + be.getErrorCode().getCode() + "] " + be.getMessage()
                            : t.getClass().getSimpleName() + ": " + t.getMessage();
                    failureTypes.add(label);
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        return new ConcurrencyResult(successCount.get(), failureTypes);
    }

    private record ConcurrencyResult(int successCount, List<String> failureTypes) {}

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
