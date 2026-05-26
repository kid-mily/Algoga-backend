package com.kidmily.algoga_server.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.application.usecase.PaymentCommandUseCase;
import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.infrastructure.portone.PortOneClient;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCommandService implements PaymentCommandUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PortOneClient portOneClient;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Long handle(CreatePaymentCommand command) {
        log.info("[PaymentCommandService] 결제 요청 - bookingId: {}, type: {}, amount: {}",
                command.bookingId(), command.paymentType(), command.amount());

        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> {
                    log.warn("[PaymentCommandService] 예약을 찾을 수 없음 - bookingId: {}", command.bookingId());
                    return new BusinessException(PaymentErrorCode.BOOKING_NOT_FOUND);
                });

        String idempotencyKey = generateIdempotencyKey(command.bookingId(), command.paymentType());
        paymentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[PaymentCommandService] 이미 완료된 결제 - idempotencyKey: {}", idempotencyKey);
                throw new BusinessException(PaymentErrorCode.DUPLICATE_PAYMENT);
            }
        });

        validateAmount(booking, command.paymentType(), command.amount());

        JsonNode portoneResult = portOneClient.getPayment(command.portonePaymentId());
        String portoneStatus = portoneResult.path("status").asText();
        int paidAmount = portoneResult.path("amount").path("total").asInt();

        log.info("[PaymentCommandService] PortOne 검증 결과 - status: {}, amount: {}", portoneStatus, paidAmount);

        if (paidAmount != command.amount()) {
            log.warn("[PaymentCommandService] 결제 금액 불일치 - 요청: {}, PortOne: {}", command.amount(), paidAmount);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Payment payment = Payment.create(
                command.bookingId(),
                command.userId(),
                command.paymentType(),
                command.amount(),
                command.usedMileage(),
                command.usedCouponId(),
                idempotencyKey
        );

        if ("PAID".equals(portoneStatus)) {
            payment.markSuccess(command.portonePaymentId());
            BookingStatus newBookingStatus = resolveBookingStatus(command.paymentType());
            bookingRepository.updateStatus(command.bookingId(), newBookingStatus);
            log.info("[PaymentCommandService] 결제 성공 - bookingId: {}, newStatus: {}", command.bookingId(), newBookingStatus);

            // 메일 발송 이벤트
            User user = userRepository.findById(command.userId())
                    .orElseThrow(() -> {
                        log.warn("[PaymentCommandService] 유저를 찾을 수 없음 - userId: {}", command.userId());
                        return new BusinessException(UserErrorCode.NOT_FOUND_USER);
                    });

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    booking.getBookingNumber(),
                    command.paymentType(),
                    command.amount(),
                    LocalDateTime.now()
            ));
        } else {
            payment.markFailed();
            log.warn("[PaymentCommandService] 결제 실패 - portoneStatus: {}", portoneStatus);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("[PaymentCommandService] 결제 저장 완료 - paymentId: {}", saved.getId());

        return saved.getId();
    }

    @Override
    public void handleWebhook(String portonePaymentId) {
        log.info("[PaymentCommandService] 웹훅 수신 - portonePaymentId: {}", portonePaymentId);

        JsonNode portoneResult = portOneClient.getPayment(portonePaymentId);
        String portoneStatus = portoneResult.path("status").asText();

        paymentRepository.findByPortonePaymentId(portonePaymentId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.info("[PaymentCommandService] 웹훅 - 이미 SUCCESS 처리된 결제, 스킵 - portonePaymentId: {}", portonePaymentId);
                return;
            }

            if ("PAID".equals(portoneStatus)) {
                payment.markSuccess(portonePaymentId);
                paymentRepository.save(payment);
                BookingStatus newBookingStatus = resolveBookingStatus(payment.getPaymentType());
                bookingRepository.updateStatus(payment.getBookingId(), newBookingStatus);
                log.info("[PaymentCommandService] 웹훅 - 결제 SUCCESS 처리 완료 - bookingId: {}", payment.getBookingId());
            } else {
                log.warn("[PaymentCommandService] 웹훅 - 결제 미완료 상태 - portoneStatus: {}", portoneStatus);
            }
        });
    }

    private void validateAmount(Booking booking, PaymentType type, int amount) {
        int expected = switch (type) {
            case DEPOSIT -> booking.getDepositPrice();
            case BALANCE -> booking.getBalancePrice();
            case FULL -> booking.getTotalPrice();
            case LECTURE_ONLY -> amount;
        };

        if (type != PaymentType.LECTURE_ONLY && expected != amount) {
            log.warn("[PaymentCommandService] 결제 금액 불일치 - 예상: {}, 요청: {}", expected, amount);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private BookingStatus resolveBookingStatus(PaymentType type) {
        return switch (type) {
            case DEPOSIT -> BookingStatus.DEPOSIT_PAID;
            case BALANCE, FULL -> BookingStatus.FULL_PAID;
            case LECTURE_ONLY -> BookingStatus.FULL_PAID;
        };
    }

    private String generateIdempotencyKey(Long bookingId, PaymentType type) {
        return bookingId + "_" + type.name();
    }
}