package com.kidmily.algoga_server.itinerary.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;

/**
 * AI 일정 추천 도메인 전용 예외. 글로벌 BusinessException 을 상속해 공통 에러 응답 포맷을 사용한다.
 */
public class ItineraryException extends BusinessException {

    public ItineraryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
