package com.kidmily.algoga_server.booking.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidmily.algoga_server.booking.application.command.CreateBookingCommand;
import com.kidmily.algoga_server.booking.application.usecase.BookingCommandUseCase;
import com.kidmily.algoga_server.booking.domain.event.BookingCanceledEvent;
import com.kidmily.algoga_server.booking.domain.event.BookingCreatedEvent;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.booking.domain.repository.BookingRepository;
import com.kidmily.algoga_server.booking.exception.BookingErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.packages.domain.model.Package;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingCommandService implements BookingCommandUseCase {

    private final BookingRepository bookingRepository;
    private final PackageRepository packageRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher; // 승재 추가

    @Override
    public Long handle(CreateBookingCommand command) {
        log.info("[BookingCommandService] 예약 생성 요청 - packageId: {}, userId: {}",
                command.packageId(), command.userId());

        // 패키지 조회
        Package pkg = packageRepository.findById(command.packageId())
                .orElseThrow(() -> {
                    log.warn("[BookingCommandService] 패키지를 찾을 수 없음 - packageId: {}", command.packageId());
                    return new BusinessException(BookingErrorCode.PACKAGE_NOT_AVAILABLE);
                });

        // 가격 계산
        int totalPrice = pkg.getTotalPrice();
        int depositPrice = (int) (totalPrice * pkg.getDepositRate().doubleValue());
        int balancePrice = totalPrice - depositPrice;

        // 예약 번호 생성 (BK-날짜-랜덤5자리)
        String bookingNumber = generateBookingNumber();

        // 항공편 정보 스냅샷 (JSON)
        String flightInfo = buildFlightInfo(pkg);

        // 예약 생성
        Booking booking = Booking.create(
                command.packageId(),
                command.userId(),
                totalPrice,
                depositPrice,
                balancePrice,
                bookingNumber,
                flightInfo,
                pkg.getDepartureDate(),
                pkg.getReturnDate(),
                pkg.getNights()
        );

        Booking savedBooking = bookingRepository.save(booking);

        // 이벤트 발행
        eventPublisher.publishEvent(new BookingCreatedEvent(
                command.userId(),
                command.packageId(),
                pkg.getDepartureDate()
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

        bookingRepository.cancel(bookingId);

        // 이벤트 발행
        eventPublisher.publishEvent(new BookingCanceledEvent(booking.getPackageId()));

        log.info("[BookingCommandService] 예약 취소 완료 - bookingId: {}", bookingId);
    }

    private String generateBookingNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 90000) + 10000;
        return "BK-" + date + "-" + random;
    }

    private String buildFlightInfo(Package pkg) {
        try {
            Map<String, Object> flightMap = new HashMap<>();
            flightMap.put("airlineCode", pkg.getAirlineCode());
            flightMap.put("airlineName", pkg.getAirlineName());
            flightMap.put("flightNumber", pkg.getFlightNumber());
            flightMap.put("departureAirport", pkg.getDepartureAirport());
            flightMap.put("arrivalAirport", pkg.getArrivalAirport());
            flightMap.put("departureDate", pkg.getDepartureDate());
            flightMap.put("arrivalTime", pkg.getArrivalTime());
            flightMap.put("returnFlightNumber", pkg.getReturnFlightNumber());
            flightMap.put("returnDepartureTime", pkg.getReturnDepartureTime());
            flightMap.put("returnDate", pkg.getReturnDate());
            return objectMapper.writeValueAsString(flightMap);
        } catch (Exception e) {
            log.warn("[BookingCommandService] 항공편 정보 직렬화 실패");
            return "{}";
        }
    }
}