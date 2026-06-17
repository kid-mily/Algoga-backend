// 경로: banner/presentation/api/validation/ValidBannerImage.java
package com.kidmily.algoga_server.banner.presentation.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BannerImageValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBannerImage {

    // 메시지 내용을 7~10:1 비율로 변경
    String message() default "배너 이미지는 5MB 미만, 7~10:1 비율의 (PNG, JPG, JPEG) 형식이어야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}