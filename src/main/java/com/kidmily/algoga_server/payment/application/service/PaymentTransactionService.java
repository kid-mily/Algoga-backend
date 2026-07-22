package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.UserCoupon;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.UserCouponRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.event.LecturePaymentCompletedEvent;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.payment.application.command.CreateBundlePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreateLecturePaymentCommand;
import com.kidmily.algoga_server.payment.application.command.CreatePaymentCommand;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentPreviewResponse;
import com.kidmily.algoga_server.payment.presentation.api.response.BundlePaymentResponse;
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

import java.time.LocalDate;
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

        rejectIfDeparturePassed(booking);

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
            // 이전 시도가 FAILED로 남아 있으면 정상적인 재시도다. idempotency_key UNIQUE 제약과
            // 충돌하지 않도록(그리고 FAILED 행이 누적되지 않도록) 기존 행을 지우고 새로 기록한다.
            log.info("[PaymentTransactionService] 실패한 이전 결제 재시도 - 기존 FAILED 행 삭제 후 재기록 - idempotencyKey: {}", idempotencyKey);
            paymentRepository.deleteByIdempotencyKey(idempotencyKey);
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
            // 이전 시도가 FAILED로 남아 있으면 정상적인 재시도다. idempotency_key UNIQUE 제약과
            // 충돌하지 않도록(그리고 FAILED 행이 누적되지 않도록) 기존 행을 지우고 새로 기록한다.
            log.info("[PaymentTransactionService] 실패한 이전 강의 결제 재시도 - 기존 FAILED 행 삭제 후 재기록 - idempotencyKey: {}", idempotencyKey);
            paymentRepository.deleteByIdempotencyKey(idempotencyKey);
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

    /**
     * 패키지+강의 통합 결제.
     * <p>
     * PortOne 결제 1회로 결제된 금액을 <b>예약 결제 1건 + 강의 결제 N건</b>으로 나눠 기록한다.
     * 각 결제행은 같은 {@code portonePaymentId}를 공유하되 idempotency_key 는 기존 규칙을 그대로 쓴다
     * (예약={bookingId}_{type}, 강의=LECTURE_{courseId}_{userId}) → 단건 결제와 중복 방지가 자연스럽게 맞물린다.
     * <p>
     * 금액 규칙: 강의는 분할 개념이 없어 항상 정가 전액, 쿠폰·마일리지는 <b>패키지분에만</b> 적용한다.
     */
    @Caching(evict = {
            @CacheEvict(value = PaymentCacheType.Const.MY_PAYMENTS, key = "#command.userId()"),
            @CacheEvict(value = PaymentCacheType.Const.ADMIN_PAYMENT_STATS, key = "'all'")
    })
    @Transactional
    public BundlePaymentResponse saveBundlePayment(CreateBundlePaymentCommand command, String portoneStatus,
                                                   int paidAmount, String paymentMethod) {
        // 통합 결제는 패키지 최초 결제(예약금/일시불)만 허용한다. 잔금(BALANCE)은 강의와 묶일 이유가 없다.
        if (command.paymentType() != PaymentType.DEPOSIT && command.paymentType() != PaymentType.FULL) {
            log.warn("[PaymentTransactionService] 통합 결제 불가 유형 - type: {}", command.paymentType());
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_TYPE);
        }

        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> {
                    log.warn("[PaymentTransactionService] 예약을 찾을 수 없음 - bookingId: {}", command.bookingId());
                    return new BusinessException(PaymentErrorCode.BOOKING_NOT_FOUND);
                });

        rejectIfDeparturePassed(booking);

        // 완강 후 예약(installmentAllowed=false)은 분할 불가 — 단건 결제와 동일 규칙
        if (!booking.isInstallmentAllowed() && command.paymentType() != PaymentType.FULL) {
            log.warn("[PaymentTransactionService] 일시불 전용 예약에 분할 결제 시도 - bookingId: {}", command.bookingId());
            throw new BusinessException(PaymentErrorCode.INSTALLMENT_NOT_ALLOWED);
        }

        // 1) 예약 결제 중복 확인
        String bookingKey = generateIdempotencyKey(command.bookingId(), command.paymentType());
        consumeExistingPayment(bookingKey, "예약");

        // 2) 강의 유효성 + 중복(이미 결제) 확인 & 정가 합산
        List<Long> courseIds = command.courseIds().stream().distinct().toList();
        int lectureAmount = 0;
        for (Long courseId : courseIds) {
            Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                    .orElseThrow(() -> {
                        log.warn("[PaymentTransactionService] 강의를 찾을 수 없음 - courseId: {}", courseId);
                        return new BusinessException(PaymentErrorCode.COURSE_NOT_FOUND);
                    });
            consumeExistingPayment(lectureIdempotencyKey(courseId, command.userId()), "강의");
            lectureAmount += course.getPrice();
        }

        // 3) 쿠폰·마일리지는 패키지분에만 적용
        int packageBase = getBaseAmount(booking, command.paymentType());
        int couponDiscount = 0;
        if (command.usedCouponId() != null) {
            UserCoupon userCoupon = validateCoupon(command.usedCouponId(), command.userId());
            couponDiscount = calculateCouponDiscount(userCoupon, packageBase);
            log.info("[PaymentTransactionService] 통합 결제 쿠폰 적용 - userCouponId: {}, 할인: {}",
                    command.usedCouponId(), couponDiscount);
        }
        if (command.usedMileage() > 0) {
            validateMileageBalance(command.userId(), command.usedMileage());
        }

        int bookingAmount = packageBase - couponDiscount - command.usedMileage();
        if (bookingAmount < 0) {
            log.warn("[PaymentTransactionService] 할인이 패키지분을 초과 - packageBase: {}, 쿠폰: {}, 마일리지: {}",
                    packageBase, couponDiscount, command.usedMileage());
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 4) 총액 검증 (요청값·PortOne 실결제액 모두 일치해야 함)
        int expectedTotal = bookingAmount + lectureAmount;
        if (expectedTotal != command.amount() || paidAmount != command.amount()) {
            log.warn("[PaymentTransactionService] 통합 결제 금액 불일치 - 기대: {}, 요청: {}, PortOne: {}",
                    expectedTotal, command.amount(), paidAmount);
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        boolean paid = "PAID".equals(portoneStatus);
        String userName = userRepository.findById(command.userId()).map(User::getName).orElse(null);

        // 5) 예약 결제행 기록
        Payment bookingPayment = Payment.create(
                command.bookingId(), null, command.userId(), command.paymentType(),
                bookingAmount, command.usedMileage(), command.usedCouponId(),
                bookingKey, paymentMethod, userName);

        if (paid) {
            bookingPayment.markSuccess(command.portonePaymentId());
            bookingRepository.updateStatus(command.bookingId(), resolveBookingStatus(command.paymentType()));

            if (command.usedCouponId() != null) {
                userCouponRepository.markUsed(command.usedCouponId(), LocalDateTime.now());
            }
            if (command.usedMileage() > 0) {
                mileageHistoryRepository.save(MileageHistory.use(
                        command.userId(), null, command.usedMileage(), "통합 결제 마일리지 사용"));
            }
        } else {
            bookingPayment.markFailed();
        }
        Long bookingPaymentId = paymentRepository.save(bookingPayment).getId();

        // 6) 강의 결제행 기록 (강의는 항상 정가 전액)
        List<Long> lecturePaymentIds = new java.util.ArrayList<>();
        LocalDateTime paidAt = LocalDateTime.now();
        for (Long courseId : courseIds) {
            Course course = courseRepository.findByIdAndDeletedFalse(courseId).orElseThrow();
            Payment lecturePayment = Payment.create(
                    null, courseId, command.userId(), PaymentType.LECTURE_ONLY,
                    course.getPrice(), 0, null,
                    lectureIdempotencyKey(courseId, command.userId()), paymentMethod, userName);

            if (paid) {
                lecturePayment.markSuccess(command.portonePaymentId());
            } else {
                lecturePayment.markFailed();
            }
            lecturePaymentIds.add(paymentRepository.save(lecturePayment).getId());
        }

        // 7) 이벤트 발행 (성공 시에만) — 예약 알림/캘린더 + 강의별 수강권 생성
        if (paid) {
            publishBundleEvents(command, booking, bookingAmount, courseIds, paidAt);
        } else {
            log.warn("[PaymentTransactionService] 통합 결제 실패 - portoneStatus: {}", portoneStatus);
        }

        log.info("[PaymentTransactionService] 통합 결제 저장 완료 - bookingPaymentId: {}, 강의 {}건, 총액: {}",
                bookingPaymentId, lecturePaymentIds.size(), command.amount());

        return new BundlePaymentResponse(
                bookingPaymentId, lecturePaymentIds, bookingAmount, lectureAmount, command.amount());
    }

    /**
     * 통합 결제 사전 검증. <b>DB를 전혀 바꾸지 않고</b> 결제 가능 여부와 청구 예정액만 계산한다.
     * <p>
     * 통합 결제 본 API({@link #saveBundlePayment})는 PortOne 결제가 <b>끝난 뒤</b> 검증하므로,
     * 이미 산 강의가 섞여 있으면 돈이 빠져나간 뒤에 거부된다(청구는 됐는데 기록은 없는 상태).
     * FE는 결제창을 띄우기 전에 이 메서드를 먼저 호출해서 걸러야 한다.
     */
    @Transactional(readOnly = true)
    public BundlePaymentPreviewResponse previewBundlePayment(Long bookingId, List<Long> courseIds, Long userId,
                                                             PaymentType paymentType, int usedMileage,
                                                             Long usedCouponId) {
        if (paymentType != PaymentType.DEPOSIT && paymentType != PaymentType.FULL) {
            return BundlePaymentPreviewResponse.blocked("INVALID_PAYMENT_TYPE",
                    "통합 결제는 예약금(DEPOSIT) 또는 일시불(FULL)만 가능합니다.", null);
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return BundlePaymentPreviewResponse.blocked("BOOKING_NOT_FOUND", "예약 정보를 찾을 수 없습니다.", null);
        }
        if (isDeparturePassed(booking)) {
            return BundlePaymentPreviewResponse.blocked("DEPARTURE_DATE_PASSED",
                    "출발일이 지난 상품은 결제할 수 없습니다.", null);
        }
        if (!booking.isInstallmentAllowed() && paymentType != PaymentType.FULL) {
            return BundlePaymentPreviewResponse.blocked("INSTALLMENT_NOT_ALLOWED",
                    "이 예약은 일시불(전액) 결제만 가능합니다.", null);
        }
        if (isAlreadyPaid(generateIdempotencyKey(bookingId, paymentType))) {
            return BundlePaymentPreviewResponse.blocked("DUPLICATE_PAYMENT",
                    "이미 결제된 예약입니다.", null);
        }

        // 강의 검증 — 이미 산 강의는 전부 모아서 알려준다(하나씩 튕기면 FE가 여러 번 호출해야 함)
        List<Long> distinctCourseIds = courseIds == null ? List.of() : courseIds.stream().distinct().toList();
        List<Long> alreadyPaid = new java.util.ArrayList<>();
        int lectureAmount = 0;
        for (Long courseId : distinctCourseIds) {
            Course course = courseRepository.findByIdAndDeletedFalse(courseId).orElse(null);
            if (course == null) {
                return BundlePaymentPreviewResponse.blocked("COURSE_NOT_FOUND",
                        "강의 정보를 찾을 수 없습니다. (courseId: " + courseId + ")", null);
            }
            if (isAlreadyPaid(lectureIdempotencyKey(courseId, userId))) {
                alreadyPaid.add(courseId);
                continue;
            }
            lectureAmount += course.getPrice();
        }
        if (!alreadyPaid.isEmpty()) {
            return BundlePaymentPreviewResponse.blocked("DUPLICATE_PAYMENT",
                    "이미 결제한 강의가 포함되어 있습니다. 해당 강의를 제외하고 다시 시도해주세요.", alreadyPaid);
        }

        // 쿠폰·마일리지는 패키지분에만 적용 (본 결제와 동일 규칙)
        int packageBase = getBaseAmount(booking, paymentType);
        int couponDiscount = 0;
        if (usedCouponId != null) {
            try {
                couponDiscount = calculateCouponDiscount(validateCoupon(usedCouponId, userId), packageBase);
            } catch (BusinessException e) {
                return BundlePaymentPreviewResponse.blocked("COUPON_INVALID", e.getMessage(), null);
            }
        }
        if (usedMileage > 0) {
            try {
                validateMileageBalance(userId, usedMileage);
            } catch (BusinessException e) {
                return BundlePaymentPreviewResponse.blocked("INSUFFICIENT_MILEAGE", e.getMessage(), null);
            }
        }

        int packageAmount = packageBase - couponDiscount - usedMileage;
        if (packageAmount < 0) {
            return BundlePaymentPreviewResponse.blocked("INVALID_PAYMENT_AMOUNT",
                    "할인 금액이 패키지 결제액을 초과합니다.", null);
        }

        log.info("[PaymentTransactionService] 통합 결제 사전 검증 통과 - bookingId: {}, 패키지: {}, 강의: {}",
                bookingId, packageAmount, lectureAmount);
        return BundlePaymentPreviewResponse.payable(packageAmount, lectureAmount);
    }

    /** 해당 멱등키로 이미 성공한 결제가 있는지 (사전 검증용 — 아무것도 지우지 않는다) */
    private boolean isAlreadyPaid(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .isPresent();
    }

    /**
     * 기존 결제행이 있으면 성공건은 중복으로 막고, 실패건은 재시도로 보고 지운다.
     * (단건 결제 경로와 동일한 규칙)
     */
    private void consumeExistingPayment(String idempotencyKey, String label) {
        paymentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[PaymentTransactionService] 이미 완료된 {} 결제 - idempotencyKey: {}", label, idempotencyKey);
                throw new BusinessException(PaymentErrorCode.DUPLICATE_PAYMENT);
            }
            log.info("[PaymentTransactionService] 실패한 이전 {} 결제 재시도 - 기존 행 삭제 - idempotencyKey: {}",
                    label, idempotencyKey);
            paymentRepository.deleteByIdempotencyKey(idempotencyKey);
        });
    }

    private String lectureIdempotencyKey(Long courseId, Long userId) {
        return "LECTURE_" + courseId + "_" + userId;
    }

    /** 통합 결제 성공 시 예약/강의 각각의 후속 이벤트를 발행한다. */
    private void publishBundleEvents(CreateBundlePaymentCommand command, Booking booking,
                                     int bookingAmount, List<Long> courseIds, LocalDateTime paidAt) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        Accommodation accommodation = accommodationRepository.findById(booking.getAccommodationId()).orElse(null);
        String accName = accommodation != null ? accommodation.getName() : null;
        String accAddress = accommodation != null ? accommodation.getAddress() : null;
        FlightSummary flight = parseFlight(booking.getFlightInfo());

        // 예약(패키지) 결제 완료 알림
        eventPublisher.publishEvent(new PaymentCompletedEvent(
                user.getId(), user.getEmail(), user.getName(),
                booking.getBookingNumber(), null, command.paymentType(), bookingAmount, paidAt,
                null, accName, accAddress,
                flight.airline(), flight.flightNumber(), flight.departureTime(), flight.arrivalTime(),
                booking.getCheckInDate(), booking.getCheckOutDate()));

        // 캘린더용 패키지 이벤트
        eventPublisher.publishEvent(new PackagePaymentCompletedEvent(
                user.getId(), booking.getAccommodationId(), booking.getId(), booking.getCheckInDate()));

        // 강의별: 결제 완료 알림 + 수강권 생성 이벤트
        for (Long courseId : courseIds) {
            String courseName = courseRepository.findByIdAndDeletedFalse(courseId)
                    .map(Course::getTitle).orElse(null);
            int coursePrice = courseRepository.findByIdAndDeletedFalse(courseId)
                    .map(Course::getPrice).orElse(0);

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    user.getId(), user.getEmail(), user.getName(),
                    "LECTURE-" + courseId, courseId, PaymentType.LECTURE_ONLY, coursePrice, paidAt,
                    courseName, null, null, null, null, null, null, null, null));

            eventPublisher.publishEvent(new LecturePaymentCompletedEvent(command.userId(), courseId, paidAt));
        }
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
        if ("PERCENT".equals(userCoupon.getDiscountType()) || "RATE".equals(userCoupon.getDiscountType())) {
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
        LocalDateTime now = LocalDateTime.now();
        List<MileageHistory> histories = mileageHistoryRepository.findByUserId(userId);
        int balance = histories.stream()
                .filter(h -> h.isAvailableAt(now))
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

    /** 출발일(checkInDate)이 이미 지난 예약인지. 지난 상품엔 예약금/잔금/통합결제 모두 막는다. */
    private boolean isDeparturePassed(Booking booking) {
        return booking.getCheckInDate() != null && booking.getCheckInDate().isBefore(LocalDate.now());
    }

    private void rejectIfDeparturePassed(Booking booking) {
        if (isDeparturePassed(booking)) {
            log.warn("[PaymentTransactionService] 출발일 지난 예약에 결제 시도 - bookingId: {}, checkInDate: {}",
                    booking.getId(), booking.getCheckInDate());
            throw new BusinessException(BookingErrorCode.DEPARTURE_DATE_PASSED);
        }
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
