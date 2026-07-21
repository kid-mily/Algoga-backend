package com.kidmily.algoga_server.itinerary.application.usecase;

import com.kidmily.algoga_server.itinerary.application.result.PurchasedTrip;
import com.kidmily.algoga_server.itinerary.application.result.SelectablePackage;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;

import java.util.List;

public interface ItineraryQueryUseCase {

    /** 내 일정 추천 이력(최신순). */
    List<Itinerary> getMyItineraries(Long userId);

    /** 내 일정 추천 단건 조회. 본인 소유가 아니면 조회 불가. */
    Itinerary getItinerary(Long userId, Long itineraryId);

    /** 일정 추천에 사용할 수 있는 "내가 구매(예약)한 여행" 목록(최신순). tripType=BOOKING 선택지 제공용. */
    List<PurchasedTrip> getPurchasedTrips(Long userId);

    /** 일정 추천에 사용할 수 있는 "전체 패키지(카탈로그)" 목록. tripType=PACKAGE 선택지 제공용(항공편 조회 없이 경량). */
    List<SelectablePackage> getSelectablePackages();
}
