package com.kidmily.algoga_server.admin.settings.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 🌟 핵심: 이 어노테이션이 불리면 내부적으로 @AuthenticationPrincipal을 수행하되,
// 만약 익명 사용자(Anonymous)라면 null을 반환하고 레퍼런스 타입이 맞을 때만 바인딩하라는 설정을 둡니다.
@AuthenticationPrincipal(expression = "#this instanceof T(com.kidmily.algoga_server.admin.settings.CustomManagerDetails) ? #this : null")
public @interface CurrentManager {
}