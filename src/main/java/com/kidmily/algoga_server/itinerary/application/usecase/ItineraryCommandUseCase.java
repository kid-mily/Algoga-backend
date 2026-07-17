package com.kidmily.algoga_server.itinerary.application.usecase;

import com.kidmily.algoga_server.itinerary.application.command.RecommendItineraryCommand;
import com.kidmily.algoga_server.itinerary.domain.model.Itinerary;

public interface ItineraryCommandUseCase {

    /** 사용자 입력 + 자동수집 맥락으로 AI 일정을 생성하고 저장한 뒤 결과를 반환한다. */
    Itinerary recommend(RecommendItineraryCommand command);
}
