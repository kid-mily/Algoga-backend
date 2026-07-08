package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.lms.domain.repository.CountryRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.refund.domain.model.RefundRequest;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.stats.application.usecase.CountryProfitStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryProfitSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ⑥ 나라별 수익성 통계 (기존 나라별 인기도 확장)
 * - 기간 내 생성 예약 기준으로 나라별 예약수/총매출/순매출/환불율/잔금전환율/취소율/점유율 집계
 * - 매출=예약결제(SUCCESS+REFUNDED, 강의 제외) gross, 순매출=gross−환불(COMPLETED), 점유율=순매출 기준
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CountryProfitStatsService implements CountryProfitStatsUseCase {

    private static final Set<PaymentType> BOOKING_TYPES =
            Set.of(PaymentType.DEPOSIT, PaymentType.BALANCE, PaymentType.FULL);
    private static final Set<PaymentStatus> PAID_STATUSES =
            Set.of(PaymentStatus.SUCCESS, PaymentStatus.REFUNDED);

    private static final int I_BOOKING = 0, I_DEPOSIT = 1, I_FULL = 2, I_CANCEL = 3, I_GROSS = 4, I_REFUND = 5;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CountryProfitResponse> getList(LocalDate from, LocalDate to) {
        return buildProfiles(from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public CountryProfitSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<CountryProfitResponse> profiles = buildProfiles(from, to);
        long totalBookings = profiles.stream().mapToLong(CountryProfitResponse::bookingCount).sum();
        long totalNet = profiles.stream().mapToLong(CountryProfitResponse::netRevenue).sum();
        long totalGross = profiles.stream().mapToLong(CountryProfitResponse::grossRevenue).sum();
        long totalRefund = totalGross - totalNet;
        double avgRefundRate = totalGross == 0 ? 0.0 : Math.round((double) totalRefund / totalGross * 10000.0) / 100.0;

        CountryProfitResponse top = profiles.stream()
                .max(Comparator.comparingLong(CountryProfitResponse::netRevenue)).orElse(null);

        return new CountryProfitSummaryResponse(
                profiles.size(), totalBookings, totalNet, avgRefundRate,
                top != null ? top.countryName() : "-", top != null ? top.share() : 0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getCsv(LocalDate from, LocalDate to) {
        List<CountryProfitResponse> profiles = buildProfiles(from, to);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println("국가,예약수,총매출,순매출,환불율(%),잔금전환율(%),취소율(%),점유율(%)");
            for (CountryProfitResponse p : profiles) {
                writer.printf("%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                        p.countryName(), p.bookingCount(), p.grossRevenue(), p.netRevenue(),
                        p.refundRate(), p.balanceConversionRate(), p.cancelRate(), p.share());
            }
        }
        return baos.toByteArray();
    }

    // ── 핵심 집계 ──────────────────────────────────────

    private List<CountryProfitResponse> buildProfiles(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        if (bookings.isEmpty()) return List.of();

        Map<Long, Long> accToCountry = accToCountry(
                bookings.stream().map(Booking::getAccommodationId).collect(Collectors.toSet()));
        Map<Long, Long> bookingToCountry = bookings.stream()
                .filter(b -> accToCountry.containsKey(b.getAccommodationId()))
                .collect(Collectors.toMap(Booking::getId, b -> accToCountry.get(b.getAccommodationId()), (a, b) -> a));

        Map<Long, long[]> agg = new HashMap<>();

        // 예약 상태 집계
        for (Booking b : bookings) {
            Long country = bookingToCountry.get(b.getId());
            if (country == null) continue;
            long[] a = agg.computeIfAbsent(country, k -> new long[6]);
            a[I_BOOKING]++;
            if (b.getStatus() == BookingStatus.DEPOSIT_PAID) a[I_DEPOSIT]++;
            else if (b.getStatus() == BookingStatus.FULL_PAID) a[I_FULL]++;
            if (b.getStatus() == BookingStatus.CANCEL_REQUESTED || b.getStatus() == BookingStatus.REFUNDED) a[I_CANCEL]++;
        }

        // 매출(gross): 예약결제 SUCCESS+REFUNDED
        List<Long> bookingIds = new ArrayList<>(bookingToCountry.keySet());
        List<Payment> payments = new ArrayList<>();
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.SUCCESS));
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.REFUNDED));
        for (Payment p : payments) {
            if (!BOOKING_TYPES.contains(p.getPaymentType()) || !PAID_STATUSES.contains(p.getStatus())) continue;
            Long country = bookingToCountry.get(p.getBookingId());
            if (country == null) continue;
            agg.computeIfAbsent(country, k -> new long[6])[I_GROSS] += p.getAmount();
        }

        // 환불: COMPLETED
        Set<Long> idSet = new HashSet<>(bookingIds);
        for (RefundRequest r : refundRepository.findAllByStatus(RefundStatus.COMPLETED)) {
            if (r.getBookingId() == null || !idSet.contains(r.getBookingId())) continue;
            Long country = bookingToCountry.get(r.getBookingId());
            if (country == null) continue;
            agg.computeIfAbsent(country, k -> new long[6])[I_REFUND] += r.getAmount();
        }

        long totalNet = agg.values().stream().mapToLong(a -> a[I_GROSS] - a[I_REFUND]).sum();
        Map<Long, Country> countryMap = countryRepository.findAllByIdIn(new ArrayList<>(agg.keySet()))
                .stream().collect(Collectors.toMap(Country::getId, c -> c));

        return agg.entrySet().stream()
                .map(e -> toResponse(e.getKey(), e.getValue(), totalNet, countryMap))
                .sorted(Comparator.comparingLong(CountryProfitResponse::netRevenue).reversed())
                .toList();
    }

    private CountryProfitResponse toResponse(Long countryId, long[] a, long totalNet, Map<Long, Country> countryMap) {
        long gross = a[I_GROSS];
        long net = gross - a[I_REFUND];
        double refundRate = gross == 0 ? 0.0 : Math.round((double) a[I_REFUND] / gross * 10000.0) / 100.0;
        long balanceDenom = a[I_DEPOSIT] + a[I_FULL];
        double balanceConv = balanceDenom == 0 ? 0.0 : Math.round((double) a[I_FULL] / balanceDenom * 10000.0) / 100.0;
        double cancelRate = a[I_BOOKING] == 0 ? 0.0 : Math.round((double) a[I_CANCEL] / a[I_BOOKING] * 10000.0) / 100.0;
        double share = totalNet == 0 ? 0.0 : Math.round((double) net / totalNet * 10000.0) / 100.0;
        String name = Optional.ofNullable(countryMap.get(countryId)).map(Country::getName).orElse("알 수 없음");
        return new CountryProfitResponse(countryId, name, a[I_BOOKING], gross, net,
                refundRate, balanceConv, cancelRate, share);
    }

    private Map<Long, Long> accToCountry(Collection<Long> accommodationIds) {
        return accommodationIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));
    }
}
