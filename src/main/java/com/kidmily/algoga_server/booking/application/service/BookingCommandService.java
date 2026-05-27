package com.kidmily.algoga_server.booking.application.service;

import com.kidmily.algoga_server.accommodation.domain.model.Accommodation;
import com.kidmily.algoga_server.accommodation.domain.repository.AccommodationRepository;
import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.application.usecase.BookingCommandUseCase;
import com.kidmily.algoga_server.booking.domain.event.BookingCanceledEvent;
import com.kidmily.algoga_server.booking.domain.event.BookingCreatedEvent;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingCommandService implements BookingCommandUseCase {

    private static final double DEPOSIT_RATE = 0.3;

    private final BookingRepository bookingRepository;
    private final ApplicationEventPublisher eventPublisher; // 승재 추가
    private final AccommodationRepository accommodationRepository;

    @Override
    public Long handle(CreateBookingCommand command) {
        log.info("[BookingCommandService] 예약 생성 요청 - accommodationId: {}, userId: {}",
                command.accommodationId(), command.userId());

        // 숙소 조회
        Accommodation accommodation = accommodationRepository.findById(command.accommodationId())
                .orElseThrow(() -> {
                    log.warn("[BookingCommandService] 숙소를 찾을 수 없음 - accommodationId: {}", command.accommodationId());
                    return new BusinessException(BookingErrorCode.PACKAGE_NOT_AVAILABLE);
                });

        // 가격 계산
        int accommodationPrice = accommodation.getPricePerNight() * accommodation.getNights();
        int totalPrice = command.flightPrice() + accommodationPrice;
        int depositPrice = (int) (totalPrice * DEPOSIT_RATE);
        int balancePrice = totalPrice - depositPrice;

        // 예약 번호 생성
        String bookingNumber = generateBookingNumber();

        // 예약 생성
        Booking booking = Booking.create(
                command.accommodationId(),
                command.userId(),
                totalPrice,
                depositPrice,
                balancePrice,
                bookingNumber,
                command.flightInfo(),
                command.checkInDate(),
                command.checkOutDate(),
                accommodation.getNights()
        );

        Booking savedBooking = bookingRepository.save(booking);

        // 이벤트 발행
        eventPublisher.publishEvent(new BookingCreatedEvent(
                command.userId(),
                command.accommodationId(),
                command.checkInDate()
        ));

        log.info("[BookingCommandService] 예약 생성 완료 - bookingId: {}, bookingNumber: {}",
                savedBooking.getId(), savedBooking.getBookingNumber());

        return savedBooking.getId();
    }

   @Override
public void cancel(Long bookingId) {
    log.info("[BookingCommandService] 예약 취소 요청 - bookingId: {}", bookingId);

    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> {
                log.warn("[BookingCommandService] 예약을 찾을 수 없음 - bookingId: {}", bookingId);
                return new BusinessException(BookingErrorCode.BOOKING_NOT_FOUND);
            });

       booking.cancel();
       bookingRepository.save(booking);

    // 이벤트 발행
       eventPublisher.publishEvent(new BookingCanceledEvent(booking.getAccommodationId()));

    log.info("[BookingCommandService] 예약 취소 완료 - bookingId: {}", bookingId);
}

    private String generateBookingNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 90000) + 10000;
        return "BK-" + date + "-" + random;
    }
}