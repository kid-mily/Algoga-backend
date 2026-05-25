package com.kidmily.algoga_server.admin.presentation.advice;

import com.kidmily.algoga_server.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 🌟 어드민(Manager) 도메인의 API 컨트롤러에서 발생하는 예외만 캐치하도록 패키지 제한
@RestControllerAdvice(basePackages = "com.kidmily.algoga_server.admin.presentation.api")
public class ManagerExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        // 인터페이스(CommonExceptionAdvice)에서 로그를 찍을 수 있도록 현재 클래스의 로거를 넘겨줍니다.
        return log;
    }

    /*
     * ManagerException은 BusinessException을 상속받으므로,
     * CommonExceptionAdvice에 구현된 default handleBusinessException() 메서드가
     * 에러 코드, 상태 코드, Trace ID 등을 포함하여 응답을 자동으로 처리해 줍니다.
     * * 만약 어드민 계층에서만 발생하는 아주 특수한 예외(BusinessException이 아닌 외부 라이브러리 예외 등)가
     * 추가로 생긴다면 이곳에 @ExceptionHandler를 작성하시면 됩니다.
     */
}