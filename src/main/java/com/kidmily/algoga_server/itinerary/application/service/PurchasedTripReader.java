package com.kidmily.algoga_server.itinerary.application.service;

import com.kidmily.algoga_server.accommodation.application.usecase.AccommodationQueryUseCase;
import com.kidmily.algoga_server.accommodation.presentation.api.response.AccommodationResponse;
import com.kidmily.algoga_server.booking.application.usecase.BookingQueryUseCase;
import com.kidmily.algoga_server.booking.domain.model.BookingStatus;
import com.kidmily.algoga_server.booking.presentation.api.response.BookingResponse;
import com.kidmily.algoga_server.country.application.usecase.MapUseCase;
import com.kidmily.algoga_server.itinerary.application.result.PurchasedTrip;
import com.kidmily.algoga_server.itinerary.exception.ItineraryErrorCode;
import com.kidmily.algoga_server.itinerary.exception.ItineraryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * "내가 구매(예약)한 여행" 을 일정 추천 관점의 {@link PurchasedTrip} 로 읽어오는 컴포넌트.
 * booking·accommodation·country 도메인의 기존 조회 use case 를 소비만 하며, 다른 도메인을 수정하지 않는다.
 * 커맨드(추천 생성 BOOKING 분기)와 쿼리(구매 목록 조회)가 함께 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchasedTripReader {

    // 일정 추천에 사용할 수 있는 예약 상태(취소요청·환불 완료는 제외).
    private static final Set<BookingStatus> USABLE_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.DEPOSIT_PAID, BookingStatus.FULL_PAID);

    private final BookingQueryUseCase bookingQueryUseCase;
    private final AccommodationQueryUseCase accommodationQueryUseCase;
    private final MapUseCase mapUseCase;

    /** 내가 구매한(추천에 사용 가능한) 여행 목록. 최신 예약이 앞에 오도록 예약 ID 내림차순. */
    public List<PurchasedTrip> listUsable(Long userId) {
        return bookingQueryUseCase.getMyBookings(userId).stream()
                .filter(b -> USABLE_STATUSES.contains(b.status()))
                .sorted((a, b) -> Long.compare(b.bookingId(), a.bookingId()))
                .map(this::toPurchasedTrip)
                .toList();
    }

    /**
     * 단건 조회. 본인 소유가 아니거나(내 예약 목록에 없음) 사용할 수 없는 상태면 ITN_007.
     * getMyBookings 로 소유권을 보장하므로 booking 도메인의 예외에 의존하지 않는다.
     */
    public PurchasedTrip getUsable(Long userId, Long bookingId) {
        return listUsable(userId).stream()
                .filter(t -> t.bookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new ItineraryException(ItineraryErrorCode.BOOKING_NOT_AVAILABLE));
    }

    private PurchasedTrip toPurchasedTrip(BookingResponse b) {
        String accommodationName = null;
        String destination = null;
        try {
            AccommodationResponse acc = accommodationQueryUseCase.getById(b.accommodationId());
            accommodationName = acc.name();
            destination = resolveCountryName(acc.countryId());
        } catch (Exception e) {
            // 숙소/국가 조회 실패는 목록 전체를 막지 않는다. 목적지는 폴백으로 채운다.
            log.warn("[구매여행] 숙소/국가 보강 실패 bookingId={} accommodationId={} : {}",
                    b.bookingId(), b.accommodationId(), e.getMessage());
        }
        if (!StringUtils.hasText(destination)) {
            destination = StringUtils.hasText(accommodationName) ? accommodationName : "예약 여행";
        }

        return new PurchasedTrip(
                b.bookingId(),
                destination,
                accommodationName,
                b.checkInDate(),
                b.checkOutDate(),
                b.nights(),
                b.totalPrice(),
                b.status().name(),
                b.bookingNumber()
        );
    }

    private String resolveCountryName(Long countryId) {
        if (countryId == null) {
            return null;
        }
        try {
            return mapUseCase.getActiveCountry(countryId).countryName();
        } catch (Exception e) {
            return null;
        }
    }
}
