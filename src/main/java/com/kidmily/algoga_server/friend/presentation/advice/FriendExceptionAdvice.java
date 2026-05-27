package com.kidmily.algoga_server.friend.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 🌟 친구(Friend) 도메인의 API 컨트롤러에서 발생하는 예외만 쏙 골라 캐치하도록 패키지 제한
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.friend.presentation.api")
public class FriendExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        // 인터페이스(CommonExceptionAdvice)에서 공통 로그를 찍을 수 있도록 현재 클래스의 로거를 넘겨줍니다.
        return log;
    }

    /*
     * FriendException은 BusinessException을 상속받으므로,
     * CommonExceptionAdvice에 구현된 default handleBusinessException() 메서드가
     * 에러 코드(FRIEND_007 등), 상태 코드(404), Trace ID 등을 포함하여 응답을 자동으로 처리해 줍니다.
     * * 만약 나중에 친구 파트에서만 발생하는 특수한 외부 예외(예: 친구 목록 엑셀 다운로드 시 탬플릿 에러 등)가
     * 생긴다면 여기에 @ExceptionHandler를 추가로 작성해 주시면 됩니다!
     */
}