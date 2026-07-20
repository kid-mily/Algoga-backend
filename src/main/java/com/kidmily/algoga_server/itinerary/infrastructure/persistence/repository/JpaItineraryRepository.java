package com.kidmily.algoga_server.itinerary.infrastructure.persistence.repository;

import com.kidmily.algoga_server.itinerary.infrastructure.persistence.entity.ItineraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaItineraryRepository extends JpaRepository<ItineraryEntity, Long> {
    List<ItineraryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
