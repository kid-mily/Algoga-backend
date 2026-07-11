package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.event.LecturePaymentCompletedEvent;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.domain.event.PackagePaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.payment.exception.PaymentErrorCode;
import com.kidmily.algoga_server.payment.settings.cache.PaymentCacheType;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentCommandService 의 트랜잭션 경계 담당 빈.
 * <p>
 * Spring 의 {@code @Transactional}/{@code @CacheEvict} 는 AOP 프록시를 통해 동작하기 때문에,
 * 같은 클래스 안에서 {@code this.method()} 형태로 호출하면(self-invocation) 프록시를 거치지 않아
 * 어노테이션이 조용히 무시된다. 이를 방지하기 위해 PortOne API 호출(트랜잭션 밖에서 수행)과
 * 실제 DB 반영 로직(트랜잭션 안에서 수행)을 별도의 빈으로 분리했다.
 * {@link PaymentCommandService} 는 이 빈을 주입받아 호출함으로써 프록시를 정상적으로 거치게 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CourseRepository courseRepository;
    private final UserCouponRepository userCouponRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final CacheManager cacheManager;
    private final AccommodationRepository accommodationRepository;
    private final ObjectMapper objectMapper;

    @Caching(evict = {
            @CacheEvict(value = PaymentCacheType.Const.MY_PAYMENTS, key = "#command.userId()"),
            @CacheEvict(value = PaymentCacheType.Const.ADMIN_PAYMENT_STATS, key = "'all'")
    })
    @Transactional
    public Long savePayment(CreatePaymentCommand command, String portoneStatus, int paidAmount, String paymentMethod) {
        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> {
                    log.warn("[PaymentTransactionService] 예약을 찾을 수 없음 - bookingId: {}", command.bookingId());
                    return new BusinessException(PaymentErrorCode.BOOKING_NOT_FOUND);
                });

        // 완강 후 예약(installmentAllowed=false)은 분할(DEPOSIT/BALANCE) 불가, 일시불(FULL)만 허용
        if (!booking.isInstallmentAllowed() && command.paymentType() != PaymentType.FULL) {
            log.warn("[PaymentTransactionService] 일시불 전용 예약에 분할 결제 시도 - bookingId: {}, type: {}",
                    command.bookingId(), command.paymentType());
            throw new BusinessException(PaymentErrorCode.INSTALLMENT_NOT_ALLOWED);
        }

        String idempotencyKey = generateIdempotencyKey(command.bookingId(), command.paymentType());
        paymentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[PaymentTransactionService] 이미 완료된 결제 - idempotencyKey: {}", idempotencyKey);
                throw new BusinessException(PaymentErrorCode.DUPLICATE_PAYMENT);
            }
        });

        int couponDiscount = 0;
        if (command.usedCouponId() != null) {
            UserCoupon userCoupon = validateCoupon(command.usedCouponId(), command.userId());
            int baseAmount = getBaseAmount(booking, command.paymentType());
            couponDiscount = calculateCouponDiscount(userCoupon, baseAmount);
            log.info("[PaymentTransactionService] 쿠폰 적용 - userCouponId: {}, 할인: {}", command.usedCouponId(), couponDiscount);
        }

        if (command.usedMileage() > 0) {
            validateMileageBalance(command.userId(), command.usedMileage());
            log.info("[PaymentTransactionService] 마일리지 적용 - userId: {}, 마일리지: {}", command.userId(), command.usedMileage());
        }

        validateAmount(booking, command.paymentType(), command.amount(), couponDiscount, command.usedMileage());

        if (paidAmount != command.amount()) {
            log.warn("[PaymentTransactionService] 결제 금액 불일치 - 요청: {}, PortOne: {}", command.amount(), paidAmount);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        String userName = userRepository.findById(command.userId())
                .map(User::getName)
                .orElse(null);

        Payment payment = Payment.create(
                command.bookingId(),
                null,
                command.userId(),
                command.paymentType(),
                command.amount(),
                command.usedMileage(),
                command.usedCouponId(),
                idempotencyKey,
                paymentMethod,
                userName
        );

        if ("PAID".equals(portoneStatus)) {
            payment.markSuccess(command.portonePaymentId());
            BookingStatus newBookingStatus = resolveBookingStatus(command.paymentType());
            bookingRepository.updateStatus(command.bookingId(), newBookingStatus);
            log.info("[PaymentTransactionService] 결제 성공 - bookingId: {}, newStatus: {}", command.bookingId(), newBookingStatus);

            if (command.usedCouponId() != null) {
                userCouponRepository.markUsed(command.usedCouponId(), LocalDateTime.now());
                log.info("[PaymentTransactionService] 쿠폰 사용 처리 - userCouponId: {}", command.usedCouponId());
            }

            if (command.usedMileage() > 0) {
                mileageHistoryRepository.save(MileageHistory.use(
                        command.userId(), null, command.usedMileage(), "결제 마일리지 사용"
                ));
                log.info("[PaymentTransactionService] 마일리지 차감 - userId: {}, 차감액: {}", command.userId(), command.usedMileage());
            }

            User user = userRepository.findById(command.userId())
                    .orElseThrow(() -> {
                        log.warn("[PaymentTransactionService] 유저를 찾을 수 없음 - userId: {}", command.userId());
                        return new BusinessException(UserErrorCode.NOT_FOUND_USER);
                    });

            // 숙소 정보 조회
            Accommodation accommodation = accommodationRepository.findById(booking.getAccommodationId())
                    .orElse(null);
            String accName = accommodation != null ? accommodation.getName() : null;
            String accAddress = accommodation != null ? accommodation.getAddress() : null;

// 항공 정보 파싱 (가는 편)
            FlightSummary flight = parseFlight(booking.getFlightInfo());

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    booking.getBookingNumber(),
                    null,
                    command.paymentType(),
                    command.amount(),
                    LocalDateTime.now(),
                    null,                     // productName (패키지는 숙소명을 별도 필드로)
                    accName,
                    accAddress,
                    flight.airline(),
                    flight.flightNumber(),
                    flight.departureTime(),
                    flight.arrivalTime(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate()
            ));
            // 캘린더용 패키지 이벤트 발행 승재 추가
            if (command.paymentType() == PaymentType.DEPOSIT || command.paymentType() == PaymentType.FULL) {
                eventPublisher.publishEvent(new PackagePaymentCompletedEvent(
                        user.getId(),
                        booking.getAccommodationId(),
                        booking.getId(),
                        booking.getCheckInDate()
                ));
                log.info("[PaymentTransactionService] 패키지 캘린더 이벤트 발행 - userId: {}, accommodationId: {}",
                        user.getId(), booking.getAccommodationId());
            }

        } else {
            payment.markFailed();
            log.warn("[PaymentTransactionService] 결제 실패 - portoneStatus: {}", portoneStatus);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("[PaymentTransactionService] 결제 저장 완료 - paymentId: {}", saved.getId());
        return saved.getId();
    }

    @Caching(evict = {
            @CacheEvict(value = PaymentCacheType.Const.MY_PAYMENTS, key = "#command.userId()"),
            @CacheEvict(value = PaymentCacheType.Const.ADMIN_PAYMENT_STATS, key = "'all'")
    })
    @Transactional
    public Long saveLecturePayment(CreateLecturePaymentCommand command, String portoneStatus, int paidAmount, String paymentMethod) {
        courseRepository.findByIdAndDeletedFalse(command.courseId())
                .orElseThrow(() -> {
                    log.warn("[PaymentTransactionService] 강의를 찾을 수 없음 - courseId: {}", command.courseId());
                    return new BusinessException(PaymentErrorCode.COURSE_NOT_FOUND);
                });

        String idempotencyKey = "LECTURE_" + command.courseId() + "_" + command.userId();
        paymentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[PaymentTransactionService] 이미 완료된 강의 결제 - courseId: {}", command.courseId());
                throw new BusinessException(PaymentErrorCode.DUPLICATE_PAYMENT);
            }
        });

        if (command.usedCouponId() != null) {
            validateCoupon(command.usedCouponId(), command.userId());
            log.info("[PaymentTransactionService] 강의 쿠폰 적용 - userCouponId: {}", command.usedCouponId());
        }

        if (command.usedMileage() > 0) {
            validateMileageBalance(command.userId(), command.usedMileage());
            log.info("[PaymentTransactionService] 강의 마일리지 적용 - userId: {}, 마일리지: {}", command.userId(), command.usedMileage());
        }

        if (paidAmount != command.amount()) {
            log.warn("[PaymentTransactionService] 강의 결제 금액 불일치 - 요청: {}, PortOne: {}", command.amount(), paidAmount);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        String userName = userRepository.findById(command.userId())
                .map(User::getName)
                .orElse(null);

        Payment payment = Payment.create(
                null,
                command.courseId(),
                command.userId(),
                PaymentType.LECTURE_ONLY,
                command.amount(),
                command.usedMileage(),
                command.usedCouponId(),
                idempotencyKey,
                paymentMethod,
                userName
        );

        if ("PAID".equals(portoneStatus)) {
            payment.markSuccess(command.portonePaymentId());
            log.info("[PaymentTransactionService] 강의 결제 성공 - courseId: {}", command.courseId());
            LocalDateTime paidAt = LocalDateTime.now();

            if (command.usedCouponId() != null) {
                userCouponRepository.markUsed(command.usedCouponId(), LocalDateTime.now());
                log.info("[PaymentTransactionService] 쿠폰 사용 처리 - userCouponId: {}", command.usedCouponId());
            }

            if (command.usedMileage() > 0) {
                mileageHistoryRepository.save(MileageHistory.use(
                        command.userId(), command.courseId(), command.usedMileage(), "강의 결제 마일리지 사용"
                ));
                log.info("[PaymentTransactionService] 마일리지 차감 - userId: {}, 차감액: {}", command.userId(), command.usedMileage());
            }

            User user = userRepository.findById(command.userId())
                    .orElseThrow(() -> {
                        log.warn("[PaymentTransactionService] 유저를 찾을 수 없음 - userId: {}", command.userId());
                        return new BusinessException(UserErrorCode.NOT_FOUND_USER);
                    });

            String courseName = courseRepository.findByIdAndDeletedFalse(command.courseId())
                    .map(Course::getTitle)
                    .orElse(null);

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    "LECTURE-" + command.courseId(),
                    command.courseId(),
                    PaymentType.LECTURE_ONLY,
                    command.amount(),
                    paidAt,
                    courseName,
                    null, null, null, null, null, null,
                    null, null
            ));

            eventPublisher.publishEvent(new LecturePaymentCompletedEvent(
                    command.userId(),
                    command.courseId(),
                    paidAt
            ));
        } else {
            payment.markFailed();
            log.warn("[PaymentTransactionService] 강의 결제 실패 - portoneStatus: {}", portoneStatus);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("[PaymentTransactionService] 강의 결제 저장 완료 - paymentId: {}", saved.getId());
        return saved.getId();
    }

    @Transactional
    public void processWebhook(String portonePaymentId, String portoneStatus, String paymentMethod) {
        paymentRepository.findByPortonePaymentId(portonePaymentId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.info("[PaymentTransactionService] 웹훅 - 이미 SUCCESS 처리된 결제, 스킵 - portonePaymentId: {}", portonePaymentId);
                return;
            }

            if ("PAID".equals(portoneStatus)) {
                payment.markSuccess(portonePaymentId);
                payment.updatePaymentMethod(paymentMethod);
                paymentRepository.save(payment);

                // 웹훅 단독으로 결제가 확정되는 경우에도 쿠폰을 사용 처리한다.
                // (savePayment/saveLecturePayment 경로에서만 markUsed 하고 있어 웹훅 확정분이 쿠폰 사용률 통계에서 누락되던 문제)
                if (payment.getUsedCouponId() != null) {
                    userCouponRepository.markUsed(payment.getUsedCouponId(), LocalDateTime.now());
                    log.info("[PaymentTransactionService] 웹훅 - 쿠폰 사용 처리 - userCouponId: {}", payment.getUsedCouponId());
                }

                // 웹훅으로 결제 확정 시에도 결제 목록/통계 캐시를 무효화한다.
                // 웹훅은 PortOne이 호출하는 경로라 userId 파라미터가 없어 @CacheEvict(key=...)를 쓸 수 없으므로,
                // 저장된 payment 엔티티의 userId 로 CacheManager 를 통해 직접 evict 한다.
                Long cacheUserId = payment.getUserId();
                if (cacheUserId != null) {
                    Cache myPaymentsCache = cacheManager.getCache(PaymentCacheType.Const.MY_PAYMENTS);
                    if (myPaymentsCache != null) {
                        myPaymentsCache.evict(cacheUserId);
                    }
                }
                Cache adminStatsCache = cacheManager.getCache(PaymentCacheType.Const.ADMIN_PAYMENT_STATS);
                if (adminStatsCache != null) {
                    adminStatsCache.evict("all");
                }

                if (payment.getBookingId() != null) {
                    BookingStatus newBookingStatus = resolveBookingStatus(payment.getPaymentType());
                    bookingRepository.updateStatus(payment.getBookingId(), newBookingStatus);
                    log.info("[PaymentTransactionService] 웹훅 - 결제 SUCCESS 처리 완료 - bookingId: {}", payment.getBookingId());
                }
            } else {
                log.warn("[PaymentTransactionService] 웹훅 - 결제 미완료 상태 - portoneStatus: {}", portoneStatus);
            }
        });
    }

    private UserCoupon validateCoupon(Long userCouponId, Long userId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.COUPON_NOT_FOUND));

        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(PaymentErrorCode.COUPON_NOT_OWNED);
        }
        if ("USED".equals(userCoupon.getStatus())) {
            throw new BusinessException(PaymentErrorCode.COUPON_ALREADY_USED);
        }
        if (userCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(PaymentErrorCode.COUPON_EXPIRED);
        }

        return userCoupon;
    }

    private int calculateCouponDiscount(UserCoupon userCoupon, int baseAmount) {
        if ("PERCENT".equals(userCoupon.getDiscountType())) {
            return baseAmount * userCoupon.getDiscountValue() / 100;
        }
        return Math.min(userCoupon.getDiscountValue(), baseAmount);
    }

    private int getBaseAmount(Booking booking, PaymentType type) {
        return switch (type) {
            case DEPOSIT -> booking.getDepositPrice();
            case BALANCE -> booking.getBalancePrice();
            case FULL -> booking.getTotalPrice();
            case LECTURE_ONLY -> 0;
        };
    }

    private void validateAmount(Booking booking, PaymentType type, int amount, int couponDiscount, int usedMileage) {
        int expected = switch (type) {
            case DEPOSIT -> booking.getDepositPrice() - couponDiscount - usedMileage;
            case BALANCE -> booking.getBalancePrice() - couponDiscount - usedMileage;
            case FULL -> booking.getTotalPrice() - couponDiscount - usedMileage;
            case LECTURE_ONLY -> amount;
        };

        if (type != PaymentType.LECTURE_ONLY && expected != amount) {
            log.warn("[PaymentTransactionService] 결제 금액 불일치 - 예상: {}, 요청: {}, 쿠폰할인: {}, 마일리지: {}", expected, amount, couponDiscount, usedMileage);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private void validateMileageBalance(Long userId, int usedMileage) {
        List<MileageHistory> histories = mileageHistoryRepository.findByUserId(userId);
        int balance = histories.stream()
                .mapToInt(h -> "EARN".equals(h.getType()) ? h.getAmount() : -h.getAmount())
                .sum();
        if (balance < usedMileage) {
            log.warn("[PaymentTransactionService] 마일리지 잔액 부족 - userId: {}, 잔액: {}, 요청: {}", userId, balance, usedMileage);
            throw new BusinessException(PaymentErrorCode.INSUFFICIENT_MILEAGE);
        }
    }

    private BookingStatus resolveBookingStatus(PaymentType type) {
        return switch (type) {
            case DEPOSIT -> BookingStatus.DEPOSIT_PAID;
            case BALANCE, FULL, LECTURE_ONLY -> BookingStatus.FULL_PAID;
        };
    }

    private String generateIdempotencyKey(Long bookingId, PaymentType type) {
        return bookingId + "_" + type.name();
    }


// 항공 JSON 파싱 헬퍼
private FlightSummary parseFlight(String flightInfoJson) {
    if (flightInfoJson == null || flightInfoJson.isBlank()) {
        return FlightSummary.EMPTY;
    }
    try {
        JsonNode node = objectMapper.readTree(flightInfoJson);
        String airline = node.hasNonNull("airline") ? node.get("airline").asText() : null;
        String flightNumber = node.hasNonNull("flightNumber") ? node.get("flightNumber").asText() : null;
        LocalDateTime dep = parseDateTime(node.hasNonNull("departureTime") ? node.get("departureTime").asText() : null);
        LocalDateTime arr = parseDateTime(node.hasNonNull("arrivalTime") ? node.get("arrivalTime").asText() : null);
        return new FlightSummary(airline, flightNumber, dep, arr);
    } catch (Exception e) {
        log.warn("[PaymentTransactionService] 항공 정보 파싱 실패 - {}", e.getMessage());
        return FlightSummary.EMPTY;
    }
}

private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    try {
        return LocalDateTime.parse(value);
    } catch (Exception e) {
        return null;
    }
}

private record FlightSummary(String airline, String flightNumber,
                             LocalDateTime departureTime, LocalDateTime arrivalTime) {
    static final FlightSummary EMPTY = new FlightSummary(null, null, null, null);
}
}
