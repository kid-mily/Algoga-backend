// inquiry/exception/InquiryException.java
package com.kidmily.algoga_server.inquiry.exception;

import com.kidmily.algoga_server.global.exception.BaseErrorCode;
import com.kidmily.algoga_server.global.exception.BusinessException;

/**
 * Inquiry(1:1 문의) 도메인 전용 커스텀 예외 클래스.
 * 글로벌 BusinessException을 상속받아 공통 에러 응답 포맷을 그대로 사용합니다.
 */
public class InquiryException extends BusinessException {
    
    public InquiryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}