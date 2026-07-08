package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.lms.domain.model.Country;
import com.kidmily.algoga_server.course.domain.repository.CountryRepository;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import com.kidmily.algoga_server.payment.domain.model.PaymentStatus;
import com.kidmily.algoga_server.payment.domain.repository.PaymentRepository;
import com.kidmily.algoga_server.stats.application.usecase.CountryStatsUseCase;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryStatsItemResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.CountryTop10Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryStatsService implements CountryStatsUseCase {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final AccommodationRepository accommodationRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CountryStatsItemResponse> getCountryStats(LocalDate from, LocalDate to, String search) {
        List<CountryStatsItemResponse> stats = buildStats(from, to);

        if (search != null && !search.isBlank()) {
            String keyword = search.toLowerCase();
            stats = stats.stream()
                    .filter(s -> s.countryName().toLowerCase().contains(keyword))
                    .toList();
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public CountryTop10Response getTop10(LocalDate from, LocalDate to) {
        List<CountryStatsItemResponse> stats = buildStats(from, to);

        List<CountryStatsItemResponse> bookingTop10 = stats.stream()
                .sorted(Comparator.comparingLong(CountryStatsItemResponse::bookingCount).reversed())
                .limit(10)
                .toList();

        List<CountryStatsItemResponse> revenueTop10 = stats.stream()
                .sorted(Comparator.comparingLong(CountryStatsItemResponse::revenue).reversed())
                .limit(10)
                .toList();

        return new CountryTop10Response(bookingTop10, revenueTop10);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getCsvExport(LocalDate from, LocalDate to) {
        List<CountryStatsItemResponse> stats = buildStats(from, to);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // BOM for Excel UTF-8
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, 0, 3);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.println("국가,회원가입 수,예약 건수,매출,점유율(%)");
            for (CountryStatsItemResponse item : stats) {
                writer.printf("%s,%d,%d,%d,%.2f%n",
                        item.countryName(),
                        item.signupCount(),
                        item.bookingCount(),
                        item.revenue(),
                        item.shareRate());
            }
        }

        return baos.toByteArray();
    }

    private List<CountryStatsItemResponse> buildStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // 기간 내 예약 목록
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);
        if (bookings.isEmpty()) {
            return List.of();
        }

        // bookingId → accommodationId 맵
        Map<Long, Long> bookingToAccommodation = bookings.stream()
                .collect(Collectors.toMap(Booking::getId, Booking::getAccommodationId));

        // accommodationId → countryId 맵 (중복 제거 후 조회)
        Set<Long> accommodationIds = new HashSet<>(bookingToAccommodation.values());
        Map<Long, Long> accommodationToCountry = accommodationIds.stream()
                .map(id -> accommodationRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Accommodation::getId, Accommodation::getCountryId));

        // 나라별 예약 건수 집계
        Map<Long, Long> bookingCountByCountry = new HashMap<>();
        for (Booking booking : bookings) {
            Long countryId = accommodationToCountry.get(booking.getAccommodationId());
            if (countryId != null) {
                bookingCountByCountry.merge(countryId, 1L, Long::sum);
            }
        }

        // 성공 결제 목록으로 나라별 매출 집계
        List<Long> bookingIds = bookings.stream().map(Booking::getId).toList();
        List<Payment> payments = paymentRepository.findByBookingIdInAndStatus(bookingIds, PaymentStatus.SUCCESS);

        Map<Long, Long> revenueByCountry = new HashMap<>();
        for (Payment payment : payments) {
            Long accommodationId = bookingToAccommodation.get(payment.getBookingId());
            if (accommodationId == null) continue;
            Long countryId = accommodationToCountry.get(accommodationId);
            if (countryId == null) continue;
            revenueByCountry.merge(countryId, (long) payment.getAmount(), Long::sum);
        }

        long totalRevenue = revenueByCountry.values().stream().mapToLong(Long::longValue).sum();

        // 국가 ID 목록 (예약 또는 매출이 있는 모든 나라) — 한 번에 조회 (N+1 방지)
        Set<Long> countryIdSet = new HashSet<>();
        countryIdSet.addAll(bookingCountByCountry.keySet());
        countryIdSet.addAll(revenueByCountry.keySet());

        List<Long> countryIds = new ArrayList<>(countryIdSet);
        Map<Long, Country> countryMap = countryRepository.findAllByIdIn(countryIds).stream()
                .collect(Collectors.toMap(Country::getId, c -> c));

        return countryIds.stream()
                .map(countryId -> {
                    Country country = countryMap.get(countryId);
                    String name = country != null ? country.getName() : "알 수 없음";
                    String code = country != null ? country.getCountryCode() : "";
                    long bookingCount = bookingCountByCountry.getOrDefault(countryId, 0L);
                    long revenue = revenueByCountry.getOrDefault(countryId, 0L);
                    return CountryStatsItemResponse.of(countryId, name, code, bookingCount, revenue, totalRevenue);
                })
                .sorted(Comparator.comparingLong(CountryStatsItemResponse::revenue).reversed())
                .toList();
    }
}
