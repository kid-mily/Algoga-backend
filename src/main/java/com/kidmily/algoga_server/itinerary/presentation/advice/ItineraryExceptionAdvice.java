package com.kidmily.algoga_server.itinerary.presentation.advice;

import com.kidmily.algoga_server.global.common.api.response.ErrorResponse;
import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import com.kidmily.algoga_server.itinerary.exception.ItineraryException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.itinerary.presentation.api")
public class ItineraryExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    @ExceptionHandler(ItineraryException.class)
    public ResponseEntity<ErrorResponse> handleItineraryException(ItineraryException e) {
        return handleBusinessException(e);
    }
}
