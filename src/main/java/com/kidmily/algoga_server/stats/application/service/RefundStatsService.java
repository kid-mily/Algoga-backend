package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.domain.repository.CountryRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.application.usecase.RefundStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ③ 환불·취소 통계
 * - 환불율/순매출은 예약 결제(DEPOSIT/BALANCE/FULL)만 분모로 함 (강의 LECTURE_ONLY는 환불 경로 없음 → 제외)
 * - 환불액은 refund_requests.status=COMPLETED 기준 (실제 나간 돈)
 * - 취소는 payments 역추적으로 3단계(미결제/선금후/완납후) 분류
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundStatsService implements RefundStatsUseCase {

    private static final Set<PaymentType> BOOKING_TYPES =
            Set.of(PaymentType.DEPOSIT, PaymentType.BALANCE, PaymentType.FULL);
    private static final Set<PaymentStatus> PAID_STATUSES =
            Set.of(PaymentStatus.SUCCESS, PaymentStatus.REFUNDED);

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public RefundSummaryResponse getSummary(LocalDate from, LocalDate to) {
        long bookingRevenue = bookingRevenue(from, to);
        List<RefundRequest> refunds = completedRefunds(from, to);
        long totalRefund = refunds.stream().mapToLong(RefundRequest::getAmount).sum();
        return RefundSummaryResponse.of(bookingRevenue, totalRefund, refunds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundTrendResponse> getTrend(LocalDate from, LocalDate to) {
        List<Payment> payments = bookingPayments(from, to);
        List<RefundRequest> refunds = completedRefunds(from, to);

        List<RefundTrendResponse> result = new ArrayList<>();
        for (YearMonth m = YearMonth.from(from); !m.isAfter(YearMonth.from(to)); m = m.plusMonths(1)) {
            final YearMonth month = m;
            long revenue = payments.stream()
                    .filter(p -> YearMonth.from(p.getCreatedAt()).equals(month))
                    .mapToLong(Payment::getAmount).sum();
            long refund = refunds.stream()
                    .filter(r -> YearMonth.from(r.getCreatedAt()).equals(month))
                    .mapToLong(RefundRequest::getAmount).sum();
            result.add(new RefundTrendResponse(month.toString(), revenue, refund, revenue - refund));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundTimingResponse> getTiming(LocalDate from, LocalDate to) {
        List<RefundRequest> refunds = completedRefunds(from, to);
        Map<Long, Booking> bookings = bookingMap(refunds.stream().map(RefundRequest::getBookingId).toList());

        long[] count = new long[3];   // [14일↑, 7~14, <7]
        long[] amount = new long[3];
        for (RefundRequest r : refunds) {
            Booking b = bookings.get(r.getBookingId());
            if (b == null || b.getCheckInDate() == null) continue;
            long daysBefore = ChronoUnit.DAYS.between(r.getCreatedAt().toLocalDate(), b.getCheckInDate());
            int idx = daysBefore >= 14 ? 0 : (daysBefore >= 7 ? 1 : 2);
            count[idx]++;
            amount[idx] += r.getAmount();
        }

        return List.of(
                new RefundTimingResponse("14일 이상", 100, count[0], amount[0]),
                new RefundTimingResponse("7~14일", 50, count[1], amount[1]),
                new RefundTimingResponse("7일 미만", 0, count[2], amount[2]));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundByCountryResponse> getByCountry(LocalDate from, LocalDate to) {
        List<RefundRequest> refunds = completedRefunds(from, to);
        Map<Long, Booking> bookings = bookingMap(refunds.stream().map(RefundRequest::getBookingId).toList());
        Map<Long, Long> accToCountry = accToCountry(
                bookings.values().stream().map(Booking::getAccommodationId).toList());

        Map<Long, long[]> byCountry = new HashMap<>(); // countryId → [refundCount, refundAmount]
        for (RefundRequest r : refunds) {
            Booking b = bookings.get(r.getBookingId());
            if (b == null) continue;
            Long countryId = accToCountry.get(b.getAccommodationId());
            if (countryId == null) continue;
            long[] agg = byCountry.computeIfAbsent(countryId, k -> new long[2]);
            agg[0]++;
            agg[1] += r.getAmount();
        }

        // 나라별 예약 건수(기간 내 생성) + 예약결제 매출(환불율 분모) 집계
        Map<Long, Long> bookingCountByCountry = bookingCountByCountry(from, to);
        Map<Long, Long> revenueByCountry = bookingRevenueByCountry(from, to);

        Map<Long, Country> countryMap = countryMap(byCountry.keySet());
        return byCountry.entrySet().stream()
                .map(e -> {
                    Long countryId = e.getKey();
                    String name = Optional.ofNullable(countryMap.get(countryId))
                            .map(Country::getName).orElse("알 수 없음");
                    return RefundByCountryResponse.of(
                            countryId, name,
                            bookingCountByCountry.getOrDefault(countryId, 0L),
                            e.getValue()[0], e.getValue()[1],
                            revenueByCountry.getOrDefault(countryId, 0L));
                })
                .sorted(Comparator.comparingLong(RefundByCountryResponse::refundAmount).reversed())
                .toList();
    }

    /** 나라별 예약 건수 (기간 내 생성된 예약 기준). */
    private Map<Long, Long> bookingCountByCountry(LocalDate from, LocalDate to) {
        List<Booking> periodBookings = bookingRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        Map<Long, Long> accToCountry = accToCountry(
                periodBookings.stream().map(Booking::getAccommodationId).toList());
        Map<Long, Long> result = new HashMap<>();
        for (Booking b : periodBookings) {
            Long countryId = accToCountry.get(b.getAccommodationId());
            if (countryId == null) continue;
            result.merge(countryId, 1L, Long::sum);
        }
        return result;
    }

    /** 나라별 예약결제 매출 (환불율 분모용, DEPOSIT/BALANCE/FULL 의 SUCCESS/REFUNDED 합). */
    private Map<Long, Long> bookingRevenueByCountry(LocalDate from, LocalDate to) {
        List<Payment> payments = bookingPayments(from, to);
        Map<Long, Booking> bookings = bookingMap(payments.stream().map(Payment::getBookingId).toList());
        Map<Long, Long> accToCountry = accToCountry(
                bookings.values().stream().map(Booking::getAccommodationId).toList());
        Map<Long, Long> result = new HashMap<>();
        for (Payment p : payments) {
            Booking b = bookings.get(p.getBookingId());
            if (b == null) continue;
            Long countryId = accToCountry.get(b.getAccommodationId());
            if (countryId == null) continue;
            result.merge(countryId, (long) p.getAmount(), Long::sum);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundReasonResponse> getReasons(LocalDate from, LocalDate to) {
        Map<String, long[]> byReason = new HashMap<>(); // reason → [count, amount]
        for (RefundRequest r : completedRefunds(from, to)) {
            String reason = (r.getReason() == null || r.getReason().isBlank()) ? "미기재" : r.getReason();
            long[] agg = byReason.computeIfAbsent(reason, k -> new long[2]);
            agg[0]++;
            agg[1] += r.getAmount();
        }
        return byReason.entrySet().stream()
                .map(e -> new RefundReasonResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(RefundReasonResponse::count).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CancelStatsResponse getCancelStats(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        long totalBookings = bookings.size();

        List<Booking> cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCEL_REQUESTED || b.getStatus() == BookingStatus.REFUNDED)
                .toList();
        List<Long> cancelledIds = cancelled.stream().map(Booking::getId).toList();

        Map<Long, Set<PaymentType>> typesByBooking = paidPaymentTypes(cancelledIds);

        long unpaid = 0, deposit = 0, full = 0;
        for (Booking b : cancelled) {
            Set<PaymentType> types = typesByBooking.getOrDefault(b.getId(), Set.of());
            if (types.contains(PaymentType.FULL)
                    || (types.contains(PaymentType.DEPOSIT) && types.contains(PaymentType.BALANCE))) {
                full++;
            } else if (types.contains(PaymentType.DEPOSIT)) {
                deposit++;
            } else {
                unpaid++;
            }
        }

        return CancelStatsResponse.of(totalBookings, unpaid, deposit, full);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────

    private List<RefundRequest> completedRefunds(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return refundRepository.findAllByStatus(RefundStatus.COMPLETED).stream()
                .filter(r -> r.getCreatedAt() != null
                        && !r.getCreatedAt().isBefore(start) && r.getCreatedAt().isBefore(end))
                .toList();
    }

    private List<Payment> bookingPayments(LocalDate from, LocalDate to) {
        return paymentRepository.findByCreatedAtBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .filter(p -> BOOKING_TYPES.contains(p.getPaymentType()) && PAID_STATUSES.contains(p.getStatus()))
                .toList();
    }

    private long bookingRevenue(LocalDate from, LocalDate to) {
        return bookingPayments(from, to).stream().mapToLong(Payment::getAmount).sum();
    }

    private Map<Long, Set<PaymentType>> paidPaymentTypes(List<Long> bookingIds) {
        if (bookingIds.isEmpty()) return Map.of();
        List<Payment> payments = new ArrayList<>();
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.SUCCESS));
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.REFUNDED));
        return payments.stream()
                .filter(p -> p.getBookingId() != null)
                .collect(Collectors.groupingBy(Payment::getBookingId,
                        Collectors.mapping(Payment::getPaymentType, Collectors.toSet())));
    }

    private Map<Long, Booking> bookingMap(Collection<Long> bookingIds) {
        return bookingIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> bookingRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Booking::getId, b -> b));
    }

    private Map<Long, Long> accToCountry(Collection<Long> accommodationIds) {
        return accommodationIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));
    }

    private Map<Long, Country> countryMap(Collection<Long> countryIds) {
        return countryRepository.findAllByIdIn(new ArrayList<>(countryIds)).stream()
                .collect(Collectors.toMap(Country::getId, c -> c));
    }
}
