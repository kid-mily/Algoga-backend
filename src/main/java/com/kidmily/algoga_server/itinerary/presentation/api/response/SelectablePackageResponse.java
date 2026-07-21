package com.kidmily.algoga_server.itinerary.presentation.api.response;

import com.kidmily.algoga_server.global.infrastructure.s3.CdnMappable;
import com.kidmily.algoga_server.itinerary.application.result.SelectablePackage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * "전체 패키지" 선택지 응답. tripType=PACKAGE 로 일정 추천 시 packageId 를 그대로 넘긴다.
 * 항공편 실시간 조회 없이 등록된 값만 담아 목록이 빠르다(구매 여행 {@code PurchasedTripResponse}와 대칭).
 */
@Schema(description = "일정 추천에 사용할 수 있는 전체 패키지(카탈로그)")
public record SelectablePackageResponse(

        @Schema(description = "패키지 ID. 추천 요청 시 tripType=PACKAGE 의 packageId 로 전송", example = "12")
        Long packageId,

        @Schema(description = "패키지명", example = "오사카 3일 자유패키지")
        String name,

        @Schema(description = "목적지(국가명). 국가 조회 실패 시 null", example = "일본")
        String destination,

        @Schema(description = "여행 시작일(체크인)", example = "2026-08-01")
        LocalDate startDate,

        @Schema(description = "여행 종료일(체크아웃)", example = "2026-08-03")
        LocalDate endDate,

        @Schema(description = "숙박 박수", example = "2")
        long nights,

        @Schema(description = "등록된 패키지 기본가(원)", example = "770000")
        int price,

        @Schema(description = "패키지 대표 이미지(CDN)")
        String imageUrl
) implements CdnMappable {

    public static SelectablePackageResponse from(SelectablePackage p) {
        return new SelectablePackageResponse(
                p.packageId(),
                p.name(),
                p.destination(),
                p.startDate(),
                p.endDate(),
                p.nights(),
                p.price(),
                p.imageUrl()
        );
    }
}
