package com.kidmily.algoga_server.itinerary.application.service;

import com.kidmily.algoga_server.course.application.result.MyCourseResult;
import com.kidmily.algoga_server.course.application.usecase.CourseUseCase;
import com.kidmily.algoga_server.itinerary.application.command.RecommendItineraryCommand;
import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryAiPort;
import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryAiResult;
import com.kidmily.algoga_server.itinerary.application.port.out.ItineraryGenerationCommand;
import com.kidmily.algoga_server.itinerary.application.result.PurchasedTrip;
import com.kidmily.algoga_server.itinerary.application.usecase.ItineraryCommandUseCase;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import com.kidmily.algoga_server.itinerary.domain.repository.ItineraryRepository;
import com.kidmily.algoga_server.itinerary.exception.ItineraryErrorCode;
import com.kidmily.algoga_server.itinerary.exception.ItineraryException;
import com.kidmily.algoga_server.packages.application.usecase.PackageQueryUseCase;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * AI 일정 추천 오케스트레이터.
 * 1) 강의 이력으로 관심 국가 수집  2) tripType 분기로 목적지·기간·패키지가격 확정
 *    (PACKAGE=packageId 조회, FREE=사용자 입력 검증)  3) Python 에 생성 위임  4) 결과 저장.
 * 여행 유형은 프론트가 명시하고, 백엔드는 packageId 유효성·자유여행 입력을 검증한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryCommandService implements ItineraryCommandUseCase {

    private static final int MAX_COURSES = 50;

    private final CourseUseCase courseUseCase;
    private final PackageQueryUseCase packageQueryUseCase;
    private final PurchasedTripReader purchasedTripReader;
    private final ItineraryAiPort itineraryAiPort;
    private final ItineraryRepository itineraryRepository;

    @Override
    @Transactional
    public Itinerary recommend(RecommendItineraryCommand command) {
        Long userId = command.userId();

        // 1) 강의 이력 → 관심 국가(강의와 매핑된 나라에 관심이 있다고 간주)
        List<String> interestedCountries = courseUseCase.getMyCourses(userId, PageRequest.of(0, MAX_COURSES))
                .getContent().stream()
                .map(MyCourseResult::countryName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2) 여행 유형 분기로 목적지/기간/패키지가격 확정
        boolean packageTrip = command.tripType().isPackageTrip();
        String destination = command.destination();
        LocalDate startDate = command.startDate();
        LocalDate endDate = command.endDate();
        Integer packagePrice = null;

        switch (command.tripType()) {
            case PACKAGE -> {
                // 전체 패키지 카탈로그: packageId 로 목적지·기간·가격을 조회해 채운다
                if (command.packageId() == null) {
                    throw new ItineraryException(ItineraryErrorCode.PACKAGE_ID_REQUIRED);
                }
                PackageResponse pkg = packageQueryUseCase.getById(command.packageId());
                destination = StringUtils.hasText(pkg.countryName()) ? pkg.countryName() : pkg.name();
                startDate = pkg.checkInDate();
                endDate = pkg.checkOutDate();
                packagePrice = pkg.totalPrice();
            }
            case BOOKING -> {
                // 구매한 예약: bookingId 로 목적지·기간·결제금액을 조회해 채운다(본인 소유만)
                if (command.bookingId() == null) {
                    throw new ItineraryException(ItineraryErrorCode.BOOKING_ID_REQUIRED);
                }
                PurchasedTrip trip = purchasedTripReader.getUsable(userId, command.bookingId());
                destination = trip.destination();
                startDate = trip.startDate();
                endDate = trip.endDate();
                packagePrice = trip.price();
            }
            case FREE -> {
                // 자유 여행: 목적지·기간 사용자 입력 필수
                if (!StringUtils.hasText(destination) || startDate == null || endDate == null) {
                    throw new ItineraryException(ItineraryErrorCode.NON_PACKAGE_INPUT_REQUIRED);
                }
            }
        }

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new ItineraryException(ItineraryErrorCode.INVALID_DATE_RANGE);
        }
        int totalDays = (int) (ChronoUnit.DAYS.between(startDate, endDate) + 1);

        // 4) Python 에 생성 위임
        ItineraryGenerationCommand genCommand = new ItineraryGenerationCommand(
                userId, destination, startDate, endDate, totalDays, command.headcount(), command.budget(),
                command.purpose(), command.companion(), command.preferences(), packageTrip, packagePrice,
                interestedCountries
        );
        ItineraryAiResult result = itineraryAiPort.recommend(genCommand);

        // 5) 결과 저장(기간·목적지는 Spring 이 확정한 값을 기준으로, 상세 일정/비용/코멘트는 AI 결과 사용)
        String finalDestination = StringUtils.hasText(result.destination()) ? result.destination() : destination;
        Itinerary itinerary = Itinerary.create(
                userId, finalDestination, startDate, endDate, totalDays, command.headcount(), packageTrip,
                command.purpose(), command.companion(), command.preferences(), command.budget(),
                result.estimatedCost(), result.days(), result.comment()
        );
        return itineraryRepository.save(itinerary);
    }
}
