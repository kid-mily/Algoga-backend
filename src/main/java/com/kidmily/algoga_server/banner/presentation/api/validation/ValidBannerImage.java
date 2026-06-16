// 경로: banner/presentation/api/validation/ValidBannerImage.java
package com.kidmily.algoga_server.banner.presentation.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BannerImageValidator.class) // 이 어노테이션의 로직을 처리할 클래스 지정
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBannerImage {

    String message() default "배너 이미지는 5MB 미만, 896x200 해상도의 (PNG, JPG, JPEG) 형식이어야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}