package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingQueryService implements BookingQueryUseCase {

    private final BookingRepository bookingRepository;

    @Override
    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("[BookingQueryService] 예약을 찾을 수 없음 - bookingId: {}", bookingId);
                    return new BusinessException(BookingErrorCode.BOOKING_NOT_FOUND);
                });
        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(Long userId) {
        log.info("[BookingQueryService] 내 예약 목록 조회 - userId: {}", userId);
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getAccommodationId(),
                booking.getUserId(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getDepositPrice(),
                booking.getBalancePrice(),
                booking.getBookingNumber(),
                booking.getFlightInfo(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.getCreatedAt()
        );
    }
}