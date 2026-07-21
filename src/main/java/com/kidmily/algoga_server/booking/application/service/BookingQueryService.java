package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import com.kidmily.algoga_server.booking.settings.cache.BookingCacheType;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingQueryService implements BookingQueryUseCase {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final PackageRepository packageRepository;

    @Override
    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("[BookingQueryService] 예약을 찾을 수 없음 - bookingId: {}", bookingId);
                    return new BusinessException(BookingErrorCode.BOOKING_NOT_FOUND);
                });
        return toResponse(booking);
    }

    @Cacheable(value = BookingCacheType.Const.MY_BOOKINGS, key = "#userId")
    @Override
    public List<BookingResponse> getMyBookings(Long userId) {
        log.info("[BookingQueryService] 내 예약 목록 조회 - userId: {}", userId);
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getMyBookingsByCountry(Long userId, Long countryId) {
        log.info("[BookingQueryService] 나라별 내 예약 목록 조회 - userId: {}, countryId: {}", userId, countryId);

        // 해당 나라에 속한 숙소 ID 집합 (booking엔 countryId가 없어 accommodation을 거쳐 매핑)
        Set<Long> accommodationIdsInCountry = accommodationRepository.findByCountryId(countryId)
                .stream()
                .map(Accommodation::getId)
                .collect(Collectors.toSet());

        if (accommodationIdsInCountry.isEmpty()) {
            return List.of();
        }

        return bookingRepository.findByUserId(userId)
                .stream()
                .filter(booking -> accommodationIdsInCountry.contains(booking.getAccommodationId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public boolean hasActiveBooking(Long userId) {
        return bookingRepository.existsByUserIdAndStatusIn(
                userId,
                List.of(BookingStatus.PENDING, BookingStatus.DEPOSIT_PAID, BookingStatus.FULL_PAID));
    }

    private BookingResponse toResponse(Booking booking) {
        // 패키지에서 예약한 건이면 패키지명을 채운다. 삭제됐거나 직접 예약이면 null.
        String packageName = booking.getPackageId() == null ? null
                : packageRepository.findById(booking.getPackageId())
                        .map(TravelPackage::getName)
                        .orElse(null);
        return new BookingResponse(
                booking.getId(),
                booking.getAccommodationId(),
                booking.getPackageId(),
                packageName,
                booking.getUserId(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getDepositPrice(),
                booking.getBalancePrice(),
                booking.getBookingNumber(),
                booking.getFlightInfo(),
                booking.getReturnFlightInfo(),
                booking.getPassengerInfo(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.isInstallmentAllowed(),
                booking.getCreatedAt()
        );
    }
}