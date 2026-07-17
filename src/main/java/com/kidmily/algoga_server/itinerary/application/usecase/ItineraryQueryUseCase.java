package com.kidmily.algoga_server.itinerary.application.usecase;

import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;

import java.util.List;

public interface ItineraryQueryUseCase {

    /** 내 일정 추천 이력(최신순). */
    List<Itinerary> getMyItineraries(Long userId);

    /** 내 일정 추천 단건 조회. 본인 소유가 아니면 조회 불가. */
    Itinerary getItinerary(Long userId, Long itineraryId);
}
