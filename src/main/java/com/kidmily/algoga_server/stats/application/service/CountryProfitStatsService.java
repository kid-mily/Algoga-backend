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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryProfitStatsService implements CountryProfitStatsUseCase {

    private static final Set<PaymentType> BOOKING_TYPES =
            Set.of(PaymentType.DEPOSIT, PaymentType.BALANCE, PaymentType.FULL);
    private static final Set<PaymentStatus> PAID_STATUSES =
            Set.of(PaymentStatus.SUCCESS, PaymentStatus.REFUNDED);

    private static final int I_BOOKING = 0;
    private static final int I_DEPOSIT = 1;
    private static final int I_FULL = 2;
    private static final int I_CANCEL = 3;
    private static final int I_GROSS = 4;
    private static final int I_REFUND = 5;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    private List<CountryProfitResponse> filterByName(List<CountryProfitResponse> profiles, String search) {
        if (search == null || search.isBlank()) {
            return profiles;
        }
        String keyword = search.strip().toLowerCase();
        return profiles.stream()
                .filter(p -> p.countryName() != null && p.countryName().toLowerCase().contains(keyword))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryProfitResponse> getList(LocalDate from, LocalDate to, String search) {
        return filterByName(buildProfiles(from, to), search);
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
                .max(Comparator.comparingLong(CountryProfitResponse::netRevenue))
                .orElse(null);

        return new CountryProfitSummaryResponse(
                profiles.size(), totalBookings, totalNet, avgRefundRate,
                top != null ? top.countryName() : "-", top != null ? top.share() : 0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getCsv(LocalDate from, LocalDate to, String search) {
        List<CountryProfitResponse> profiles = filterByName(buildProfiles(from, to), search);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println("국가,예약수,총매출,순매출,환불률(%),잔금전환율(%),취소율(%),점유율(%)");
            for (CountryProfitResponse p : profiles) {
                writer.printf("%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                        p.countryName(), p.bookingCount(), p.grossRevenue(), p.netRevenue(),
                        p.refundRate(), p.balanceConversionRate(), p.cancelRate(), p.share());
            }
        }
        return baos.toByteArray();
    }

    private List<CountryProfitResponse> buildProfiles(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> accToCountry = accToCountry(
                bookings.stream().map(Booking::getAccommodationId).collect(Collectors.toSet()));
        Map<Long, Long> bookingToCountry = bookings.stream()
                .filter(b -> accToCountry.containsKey(b.getAccommodationId()))
                .collect(Collectors.toMap(Booking::getId, b -> accToCountry.get(b.getAccommodationId()), (a, b) -> a));

        Map<Long, long[]> agg = new HashMap<>();

        for (Booking booking : bookings) {
            Long country = bookingToCountry.get(booking.getId());
            if (country == null) {
                continue;
            }
            long[] value = agg.computeIfAbsent(country, ignored -> new long[6]);
            value[I_BOOKING]++;
            if (booking.getStatus() == BookingStatus.DEPOSIT_PAID) {
                value[I_DEPOSIT]++;
            } else if (booking.getStatus() == BookingStatus.FULL_PAID) {
                value[I_FULL]++;
            }
            if (booking.getStatus() == BookingStatus.CANCEL_REQUESTED || booking.getStatus() == BookingStatus.REFUNDED) {
                value[I_CANCEL]++;
            }
        }

        List<Long> bookingIds = new ArrayList<>(bookingToCountry.keySet());
        List<Payment> payments = new ArrayList<>();
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.SUCCESS));
        payments.addAll(paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.REFUNDED));
        for (Payment payment : payments) {
            if (!BOOKING_TYPES.contains(payment.getPaymentType()) || !PAID_STATUSES.contains(payment.getStatus())) {
                continue;
            }
            Long country = bookingToCountry.get(payment.getBookingId());
            if (country == null) {
                continue;
            }
            agg.computeIfAbsent(country, ignored -> new long[6])[I_GROSS] += payment.getAmount();
        }

        Set<Long> periodBookingIds = new HashSet<>(bookingIds);
        for (RefundRequest refund : refundRepository.findAllByStatus(RefundStatus.COMPLETED)) {
            if (refund.getBookingId() == null || !periodBookingIds.contains(refund.getBookingId())) {
                continue;
            }
            if (refund.getCreatedAt() == null || refund.getCreatedAt().isBefore(start) || !refund.getCreatedAt().isBefore(end)) {
                continue;
            }
            Long country = bookingToCountry.get(refund.getBookingId());
            if (country == null) {
                continue;
            }
            agg.computeIfAbsent(country, ignored -> new long[6])[I_REFUND] += refund.getAmount();
        }

        long totalNet = agg.values().stream().mapToLong(a -> a[I_GROSS] - a[I_REFUND]).sum();
        Map<Long, Country> countryMap = countryRepository.findAllByIdIn(new ArrayList<>(agg.keySet()))
                .stream()
                .collect(Collectors.toMap(Country::getId, country -> country));

        return agg.entrySet().stream()
                .map(entry -> toResponse(entry.getKey(), entry.getValue(), totalNet, countryMap))
                .sorted(Comparator.comparingLong(CountryProfitResponse::netRevenue).reversed())
                .toList();
    }

    private CountryProfitResponse toResponse(Long countryId, long[] value, long totalNet, Map<Long, Country> countryMap) {
        long gross = value[I_GROSS];
        long net = gross - value[I_REFUND];
        double refundRate = gross == 0 ? 0.0 : Math.round((double) value[I_REFUND] / gross * 10000.0) / 100.0;
        long balanceDenom = value[I_DEPOSIT] + value[I_FULL];
        double balanceConversion = balanceDenom == 0 ? 0.0 : Math.round((double) value[I_FULL] / balanceDenom * 10000.0) / 100.0;
        double cancelRate = value[I_BOOKING] == 0 ? 0.0 : Math.round((double) value[I_CANCEL] / value[I_BOOKING] * 10000.0) / 100.0;
        double share = totalNet == 0 ? 0.0 : Math.round((double) net / totalNet * 10000.0) / 100.0;
        String name = Optional.ofNullable(countryMap.get(countryId)).map(Country::getName).orElse("알 수 없음");
        return new CountryProfitResponse(countryId, name, value[I_BOOKING], gross, net,
                refundRate, balanceConversion, cancelRate, share);
    }

    private Map<Long, Long> accToCountry(Collection<Long> accommodationIds) {
        return accommodationIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));
    }
}