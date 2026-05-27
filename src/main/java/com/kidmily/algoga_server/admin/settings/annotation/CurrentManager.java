package com.kidmily.algoga_server.admin.settings.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 🔥 instanceof를 제거하여 DevTools 충돌을 방지하고, 더 안전한 방식으로 ID를 추출합니다.
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : id")
public @interface CurrentManager {
}