package com.kidmily.algoga_server.itinerary.application.service;

import com.kidmily.algoga_server.country.application.usecase.MapUseCase;
import com.kidmily.algoga_server.itinerary.application.result.SelectablePackage;
import com.kidmily.algoga_server.packages.domain.model.TravelPackage;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 일정 추천(tripType=PACKAGE) 선택지용 "전체 패키지" 목록을 읽어오는 컴포넌트.
 * packages 도메인을 수정하지 않고 {@link PackageRepository}(읽기)와 국가명 조회({@link MapUseCase})만 소비한다.
 *
 * <p>packages 도메인의 목록 조회(PackageQueryService)는 패키지마다 외부 항공 API를 실시간 호출해 느리므로,
 * 여기서는 항공편을 조회하지 않고 등록된 값만으로 가볍게 구성한다(구매 여행 {@link PurchasedTripReader}와 대칭).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SelectablePackageReader {

    private final PackageRepository packageRepository;
    private final MapUseCase mapUseCase;

    /** 전체 패키지 선택 목록(항공편 조회 없음). */
    public List<SelectablePackage> listAll() {
        return packageRepository.findAll().stream()
                .map(this::toSelectable)
                .toList();
    }

    private SelectablePackage toSelectable(TravelPackage p) {
        long nights = (p.getCheckInDate() != null && p.getCheckOutDate() != null)
                ? ChronoUnit.DAYS.between(p.getCheckInDate(), p.getCheckOutDate())
                : 0;
        return new SelectablePackage(
                p.getId(),
                p.getName(),
                resolveCountryName(p.getCountryId()),
                p.getCheckInDate(),
                p.getCheckOutDate(),
                nights,
                p.getPrice(),
                p.getImageUrl()
        );
    }

    private String resolveCountryName(Long countryId) {
        if (countryId == null) {
            return null;
        }
        try {
            return mapUseCase.getActiveCountry(countryId).countryName();
        } catch (Exception e) {
            // 국가 조회 실패는 목록 전체를 막지 않는다(목적지만 null 로 둔다).
            log.warn("[전체패키지] 국가명 조회 실패 countryId={} : {}", countryId, e.getMessage());
            return null;
        }
    }
}
