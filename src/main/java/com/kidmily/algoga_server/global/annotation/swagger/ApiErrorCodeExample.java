package com.kidmily.algoga_server.global.annotation.swagger;



import com.kidmily.algoga_server.global.exception.BaseErrorCode;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorCodeExamples.class) // 여러 개를 중복해서 달 수 있도록 설정
public @interface ApiErrorCodeExample {
    Class<? extends BaseErrorCode> domain(); // 도메인 Enum 클래스 (예: UserErrorCode.class)
    String[] value();                        // 발생할 에러 코드 이름들 (예: {"USER_NOT_FOUND"})
}