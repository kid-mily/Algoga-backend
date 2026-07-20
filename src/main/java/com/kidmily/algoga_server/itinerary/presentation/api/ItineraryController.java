package com.kidmily.algoga_server.itinerary.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.itinerary.application.command.RecommendItineraryCommand;
import com.kidmily.algoga_server.itinerary.application.usecase.ItineraryCommandUseCase;
import com.kidmily.algoga_server.itinerary.application.usecase.ItineraryQueryUseCase;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;
import com.kidmily.algoga_server.itinerary.exception.ItineraryErrorCode;
import com.kidmily.algoga_server.itinerary.presentation.api.request.RecommendItineraryRequest;
import com.kidmily.algoga_server.itinerary.presentation.api.response.ItineraryResponse;
import com.kidmily.algoga_server.itinerary.presentation.api.response.ItinerarySummaryResponse;
import com.kidmily.algoga_server.itinerary.presentation.api.response.PurchasedTripResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
@Tag(name = "Itinerary", description = "AI 일정 추천 도메인 API")
public class ItineraryController {

    private final ItineraryCommandUseCase commandUseCase;
    private final ItineraryQueryUseCase queryUseCase;

    @PostMapping("/recommend")
    @Operation(summary = "AI 일정 추천 생성",
            description = "사용자 입력(취향/목적/동행/예산/인원)과 자동수집(예약·패키지·강의)으로 일자별 일정을 생성해 저장하고 반환합니다. "
                    + "패키지 여행 여부는 서버가 자동 판별하며, 패키지가 아니면 목적지·여행기간을 입력해야 합니다.")
    @ApiErrorCodeExample(domain = ItineraryErrorCode.class, value = {
            "NON_PACKAGE_INPUT_REQUIRED",
            "INVALID_DATE_RANGE",
            "PACKAGE_ID_REQUIRED",
            "BOOKING_ID_REQUIRED",
            "BOOKING_NOT_AVAILABLE",
            "AI_SERVER_ERROR"
    })
    public ResponseEntity<ApiResponse<ItineraryResponse>> recommend(
            @Valid @RequestBody RecommendItineraryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        RecommendItineraryCommand command = new RecommendItineraryCommand(
                userId, request.tripType(), request.packageId(), request.bookingId(), request.destination(),
                request.startDate(), request.endDate(),
                request.preferences(), request.purpose(), request.companion(), request.budget(), request.headcount()
        );
        Itinerary itinerary = commandUseCase.recommend(command);
        return ResponseEntity.ok(ApiResponse.success("ITINERARY_CREATED", "AI 일정 추천이 생성되었습니다.",
                ItineraryResponse.from(itinerary)));
    }

    @GetMapping("/purchased-trips")
    @Operation(summary = "내 구매(예약) 여행 목록",
            description = "일정 추천에 사용할 수 있는 내 구매(예약) 여행을 최신순으로 조회합니다. "
                    + "tripType=BOOKING 선택지 제공용이며, 응답의 bookingId 를 추천 요청에 그대로 전달합니다. "
                    + "(취소요청·환불 완료 예약은 제외)")
    public ResponseEntity<ApiResponse<List<PurchasedTripResponse>>> getPurchasedTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<PurchasedTripResponse> list = queryUseCase.getPurchasedTrips(userId).stream()
                .map(PurchasedTripResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("PURCHASED_TRIPS_LOADED", "구매 여행 목록 조회 성공", list));
    }

    @GetMapping
    @Operation(summary = "내 AI 일정 추천 목록", description = "내가 생성한 일정 추천 이력을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<ItinerarySummaryResponse>>> getMyItineraries(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<ItinerarySummaryResponse> list = queryUseCase.getMyItineraries(userId).stream()
                .map(ItinerarySummaryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("ITINERARIES_LOADED", "일정 추천 목록 조회 성공", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "AI 일정 추천 상세", description = "일정 추천 단건을 조회합니다. 본인 소유만 조회 가능합니다.")
    @ApiErrorCodeExample(domain = ItineraryErrorCode.class, value = {"ITINERARY_NOT_FOUND"})
    public ResponseEntity<ApiResponse<ItineraryResponse>> getItinerary(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        Itinerary itinerary = queryUseCase.getItinerary(userId, id);
        return ResponseEntity.ok(ApiResponse.success("ITINERARY_LOADED", "일정 추천 상세 조회 성공",
                ItineraryResponse.from(itinerary)));
    }
}
