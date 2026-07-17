package com.kidmily.algoga_server.itinerary.domain.repository;

import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository {
    Itinerary save(Itinerary itinerary);
    Optional<Itinerary> findById(Long itineraryId);
    List<Itinerary> findByUserId(Long userId);
}
