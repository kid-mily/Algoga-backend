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
import com.kidmily.algoga_server.stats.application.usecase.BalanceStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceAgingResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.BalanceSummaryResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.UnpaidBookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ② 잔금·미수금 통계
 * - 계약금(DEPOSIT)만 내고 잔금(BALANCE) 미납된 예약 = 매출 누수 분석
 * - booking.status(DEPOSIT_PAID/FULL_PAID) + payments(결제일) 를 in-memory 집계 (신규 테이블/쿼리 없음)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceStatsService implements BalanceStatsUseCase {

    private static final int RISK_DAYS = 14;          // 계약금 후 이 일수 초과 미납 = 이탈 위험 (환불정책 14일 경계와 일치)
    private static final int DDAY_IMMINENT_DAYS = 7;  // 체크인 이 일수 이내 미납 = 임박 (환불정책 7일 경계와 일치)
    private static final int AGING_MAX_DAYS = 30;      // 생존곡선 x축 최대 경과일

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public BalanceSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingsInPeriod(from, to);
        List<Booking> depositPaid = filterByStatus(bookings, BookingStatus.DEPOSIT_PAID);
        long fullPaidCount = filterByStatus(bookings, BookingStatus.FULL_PAID).size();

        long outstanding = depositPaid.stream().mapToLong(Booking::getBalancePrice).sum();

        Map<Long, LocalDateTime> depositDates = depositPaymentDates(depositPaid);
        LocalDate today = LocalDate.now();

        long atRisk = depositPaid.stream().filter(b -> {
            LocalDateTime d = depositDates.get(b.getId());
            return d != null && ChronoUnit.DAYS.between(d.toLocalDate(), today) > RISK_DAYS;
        }).count();

        long ddayImminent = depositPaid.stream().filter(b -> {
            LocalDate checkIn = b.getCheckInDate();
            if (checkIn == null) return false;
            long daysToCheckIn = ChronoUnit.DAYS.between(today, checkIn);
            return daysToCheckIn >= 0 && daysToCheckIn <= DDAY_IMMINENT_DAYS;
        }).count();

        return BalanceSummaryResponse.of(depositPaid.size(), fullPaidCount, outstanding, atRisk, ddayImminent);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceAgingResponse getAging(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingsInPeriod(from, to);
        List<Booking> balanceStage = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.DEPOSIT_PAID || b.getStatus() == BookingStatus.FULL_PAID)
                .toList();

        List<Payment> successPayments = successPaymentsOf(balanceStage);
        Map<Long, LocalDateTime> depositDates = paymentDatesByType(successPayments, PaymentType.DEPOSIT);
        Map<Long, LocalDateTime> balanceDates = paymentDatesByType(successPayments, PaymentType.BALANCE);

        // 계약금(DEPOSIT)을 낸 예약만이 잔금 납부 곡선의 모집단 (FULL 단건결제는 계약금 단계 없음 → 제외)
        List<Long> depositPayerIds = depositDates.keySet().stream().toList();
        long total = depositPayerIds.size();

        List<Long> daysToPay = depositPayerIds.stream()
                .filter(balanceDates::containsKey)
                .map(id -> Math.max(0, ChronoUnit.DAYS.between(
                        depositDates.get(id).toLocalDate(), balanceDates.get(id).toLocalDate())))
                .toList();

        List<BalanceAgingResponse.AgingPoint> curve = new ArrayList<>();
        for (int d = 0; d <= AGING_MAX_DAYS; d++) {
            final int day = d;
            long paidWithin = daysToPay.stream().filter(dp -> dp <= day).count();
            double rate = total == 0 ? 0.0 : Math.round((double) paidWithin / total * 10000.0) / 100.0;
            curve.add(new BalanceAgingResponse.AgingPoint(d, rate));
        }

        return new BalanceAgingResponse(curve, countryBalance(balanceStage));
    }

    /** 검색어 매칭(대소문자 무시, 공백 제거). 검색어가 없으면 전부 통과. */
    private boolean matches(String search, String... targets) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String keyword = search.strip().toLowerCase();
        for (String target : targets) {
            if (target != null && target.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnpaidBookingResponse> getUnpaidList(LocalDate from, LocalDate to, String search) {
        List<Booking> depositPaid = filterByStatus(bookingsInPeriod(from, to), BookingStatus.DEPOSIT_PAID);
        Map<Long, Payment> depositPayments = depositPaymentByBooking(depositPaid);
        Map<Long, String> accommodationNames = accommodationNames(depositPaid);
        LocalDate today = LocalDate.now();

        return depositPaid.stream()
                .map(b -> {
                    Payment dp = depositPayments.get(b.getId());
                    LocalDate depositDate = dp != null ? dp.getCreatedAt().toLocalDate() : null;
                    String userName = dp != null ? dp.getUserName() : null;
                    long elapsed = depositDate != null ? ChronoUnit.DAYS.between(depositDate, today) : 0;
                    Long dday = b.getCheckInDate() != null ? ChronoUnit.DAYS.between(today, b.getCheckInDate()) : null;
                    return new UnpaidBookingResponse(
                            b.getBookingNumber(), userName, accommodationNames.get(b.getAccommodationId()),
                            b.getBalancePrice(), depositDate, elapsed, b.getCheckInDate(), dday);
                })
                .filter(r -> matches(search, r.userName(), r.productName()))
                .sorted(Comparator.comparing(UnpaidBookingResponse::dday,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getUnpaidCsv(LocalDate from, LocalDate to, String search) {
        List<UnpaidBookingResponse> rows = getUnpaidList(from, to, search);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3); // Excel UTF-8 BOM

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println("예약번호,고객명,상품명,잔금액,계약금일,경과일,체크인일,D-day");
            for (UnpaidBookingResponse r : rows) {
                writer.printf("%s,%s,%s,%d,%s,%d,%s,%s%n",
                        nvl(r.bookingNumber()), nvl(r.userName()), nvl(r.productName()), r.balanceAmount(),
                        r.depositPaidDate() == null ? "" : r.depositPaidDate(),
                        r.daysElapsed(),
                        r.checkInDate() == null ? "" : r.checkInDate(),
                        r.dday() == null ? "" : ("D-" + r.dday()));
            }
        }
        return baos.toByteArray();
    }

    // ── 내부 헬퍼 ──────────────────────────────────────

    private List<Booking> bookingsInPeriod(LocalDate from, LocalDate to) {
        return bookingRepository.findByCreatedAtBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    private List<Booking> filterByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).toList();
    }

    private List<Payment> successPaymentsOf(List<Booking> bookings) {
        List<Long> ids = bookings.stream().map(Booking::getId).toList();
        if (ids.isEmpty()) return List.of();
        return paymentRepository.findByBookingIdInAndStatus(ids, PaymentStatus.SUCCESS);
    }

    /** DEPOSIT_PAID 예약들의 계약금 결제일 (bookingId → 최초 DEPOSIT 결제 시각) */
    private Map<Long, LocalDateTime> depositPaymentDates(List<Booking> depositPaid) {
        return paymentDatesByType(successPaymentsOf(depositPaid), PaymentType.DEPOSIT);
    }

    private Map<Long, LocalDateTime> paymentDatesByType(List<Payment> payments, PaymentType type) {
        return payments.stream()
                .filter(p -> p.getPaymentType() == type && p.getBookingId() != null)
                .collect(Collectors.toMap(
                        Payment::getBookingId,
                        Payment::getCreatedAt,
                        (a, b) -> a.isBefore(b) ? a : b));
    }

    private Map<Long, Payment> depositPaymentByBooking(List<Booking> depositPaid) {
        return successPaymentsOf(depositPaid).stream()
                .filter(p -> p.getPaymentType() == PaymentType.DEPOSIT && p.getBookingId() != null)
                .collect(Collectors.toMap(
                        Payment::getBookingId,
                        p -> p,
                        (a, b) -> a.getCreatedAt().isBefore(b.getCreatedAt()) ? a : b));
    }

    /** 예약들의 숙소명 (accommodationId → name), 미납 리스트 상품명 표시용 */
    private Map<Long, String> accommodationNames(List<Booking> bookings) {
        return bookings.stream().map(Booking::getAccommodationId).filter(Objects::nonNull).distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getName, (a, b) -> a));
    }

    /** 나라별 잔금 전환율 (DEPOSIT_PAID/FULL_PAID를 accommodation→country로 매핑) */
    private List<BalanceAgingResponse.CountryBalance> countryBalance(List<Booking> balanceStage) {
        if (balanceStage.isEmpty()) return List.of();

        Set<Long> accommodationIds = balanceStage.stream()
                .map(Booking::getAccommodationId).collect(Collectors.toSet());
        Map<Long, Long> accToCountry = accommodationIds.stream()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));

        Map<Long, long[]> byCountry = new HashMap<>(); // countryId → [depositPaid, fullPaid]
        for (Booking b : balanceStage) {
            Long countryId = accToCountry.get(b.getAccommodationId());
            if (countryId == null) continue;
            long[] c = byCountry.computeIfAbsent(countryId, k -> new long[2]);
            if (b.getStatus() == BookingStatus.DEPOSIT_PAID) c[0]++;
            else if (b.getStatus() == BookingStatus.FULL_PAID) c[1]++;
        }

        Map<Long, Country> countryMap = countryRepository.findAllByIdIn(new ArrayList<>(byCountry.keySet()))
                .stream().collect(Collectors.toMap(Country::getId, c -> c));

        return byCountry.entrySet().stream()
                .map(e -> {
                    Country country = countryMap.get(e.getKey());
                    String name = country != null ? country.getName() : "알 수 없음";
                    return BalanceAgingResponse.CountryBalance.of(e.getKey(), name, e.getValue()[0], e.getValue()[1]);
                })
                .sorted(Comparator.comparingDouble(BalanceAgingResponse.CountryBalance::conversionRate).reversed())
                .toList();
    }

    private String nvl(String s) {
        return s == null ? "-" : s;
    }
}
