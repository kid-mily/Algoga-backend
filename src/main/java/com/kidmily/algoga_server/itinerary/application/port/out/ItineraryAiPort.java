package com.kidmily.algoga_server.itinerary.application.port.out;

/**
 * AI 일정 생성 포트. 실제 생성(LLM)은 외부 Python 서버가 수행한다.
 */
public interface ItineraryAiPort {

    /** 취합된 입력으로 일자별 일정을 생성한다. */
    ItineraryAiResult recommend(ItineraryGenerationCommand command);
}
