package com.kidmily.algoga_server.itinerary.domain.model;

/**
 * 예상 비용. 패키지 여행 가격(조회값) + 음식 비용(AI 추정)으로 구성한다.
 *
 * @param packagePrice   패키지 여행 조회 가격. 패키지가 없으면(자유여행 등) null
 * @param foodCost       AI 가 추정한 여행 기간 총 음식 비용
 * @param totalEstimated 예상 총액 (packagePrice(없으면 0) + foodCost)
 */
public record EstimatedCost(
        Integer packagePrice,
        int foodCost,
        int totalEstimated
) {}
