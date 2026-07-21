package com.kidmily.algoga_server.itinerary.application.service;

import com.kidmily.algoga_server.itinerary.application.result.PurchasedTrip;
import com.kidmily.algoga_server.itinerary.application.result.SelectablePackage;
import com.kidmily.algoga_server.itinerary.application.usecase.ItineraryQueryUseCase;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import com.kidmily.algoga_server.itinerary.domain.repository.ItineraryRepository;
import com.kidmily.algoga_server.itinerary.exception.ItineraryErrorCode;
import com.kidmily.algoga_server.itinerary.exception.ItineraryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryQueryService implements ItineraryQueryUseCase {

    private final ItineraryRepository itineraryRepository;
    private final PurchasedTripReader purchasedTripReader;
    private final SelectablePackageReader selectablePackageReader;

    @Override
    public List<Itinerary> getMyItineraries(Long userId) {
        return itineraryRepository.findByUserId(userId);
    }

    @Override
    public Itinerary getItinerary(Long userId, Long itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ItineraryException(ItineraryErrorCode.ITINERARY_NOT_FOUND));
        // 본인 소유가 아니면 존재를 노출하지 않고 NOT_FOUND 로 처리
        if (!itinerary.getUserId().equals(userId)) {
            throw new ItineraryException(ItineraryErrorCode.ITINERARY_NOT_FOUND);
        }
        return itinerary;
    }

    @Override
    public List<PurchasedTrip> getPurchasedTrips(Long userId) {
        return purchasedTripReader.listUsable(userId);
    }

    @Override
    public List<SelectablePackage> getSelectablePackages() {
        // 항공편 실시간 조회 없이 등록된 값만으로 구성(전체 패키지 조회 지연 회피). packages 도메인은 수정하지 않는다.
        return selectablePackageReader.listAll();
    }
}
