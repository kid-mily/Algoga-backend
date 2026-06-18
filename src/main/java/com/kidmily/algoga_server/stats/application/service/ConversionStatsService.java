package com.kidmily.algoga_server.stats.application.service;

import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.stats.application.usecase.ConversionStatsUseCase;
import com.kidmily.algoga_server.stats.domain.model.PaymentAttempt;
import com.kidmily.algoga_server.stats.domain.repository.PaymentAttemptRepository;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionDailyResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionProductResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionProductStatsResponse;
import com.kidmily.algoga_server.stats.presentation.api.response.ConversionSummaryResponse;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionStatsService implements ConversionStatsUseCase {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final Counter paymentAttemptTotal;

    @Override
    @Transactional
    public void recordAttempt(Long userId, Long accommodationId) {
        paymentAttemptRepository.save(PaymentAttempt.create(userId, accommodationId));
        paymentAttemptTotal.increment();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionSummaryResponse getSummary(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        long attemptCount = paymentAttemptRepository.countByCreatedAtBetween(start, end);
        long completedCount = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.FULL_PAID, start, end);

        return ConversionSummaryResponse.of(attemptCount, completedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversionDailyResponse> getDailyStats(LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    LocalDateTime start = date.atStartOfDay();
                    LocalDateTime end = date.plusDays(1).atStartOfDay();
                    long attempts = paymentAttemptRepository.countByCreatedAtBetween(start, end);
                    long completed = bookingRepository.countByStatusAndCreatedAtBetween(BookingStatus.FULL_PAID, start, end);
                    return ConversionDailyResponse.of(date, attempts, completed);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionProductStatsResponse getProductStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<Long> accommodationIds = paymentAttemptRepository.findDistinctAccommodationIdsByCreatedAtBetween(start, end);

        List<ConversionProductResponse> products = accommodationIds.stream()
                .map(accommodationId -> {
                    String name = accommodationRepository.findById(accommodationId)
                            .map(a -> a.getName())
                            .orElse("알 수 없음");
                    long attempts = paymentAttemptRepository.countByAccommodationIdAndCreatedAtBetween(accommodationId, start, end);
                    long completed = bookingRepository.countByAccommodationIdAndStatusAndCreatedAtBetween(accommodationId, BookingStatus.FULL_PAID, start, end);
                    return ConversionProductResponse.of(accommodationId, name, attempts, completed);
                })
                .sorted(Comparator.comparingDouble(ConversionProductResponse::conversionRate).reversed())
                .toList();

        List<ConversionProductResponse> top = products.stream().limit(5).toList();
        List<ConversionProductResponse> bottom = products.stream()
                .sorted(Comparator.comparingDouble(ConversionProductResponse::conversionRate))
                .limit(5)
                .toList();

        return new ConversionProductStatsResponse(products, top, bottom);
    }
}
