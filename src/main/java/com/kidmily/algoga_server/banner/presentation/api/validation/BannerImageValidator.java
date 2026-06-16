// 경로: banner/presentation/api/validation/BannerImageValidator.java
package com.kidmily.algoga_server.banner.presentation.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BannerImageValidator implements ConstraintValidator<ValidBannerImage, MultipartFile> {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // 파일이 없는 경우는 @NotNull 같은 다른 어노테이션으로 처리하거나 통과시킴 (수정 API 등을 고려)
        if (file == null || file.isEmpty()) {
            return true; 
        }

        context.disableDefaultConstraintViolation();

        // 1. 용량 검사
        if (file.getSize() >= MAX_FILE_SIZE) {
            addConstraintViolation(context, "배너 이미지의 크기는 5MB 미만이어야 합니다.");
            return false;
        }

        // 2. 해상도 및 형식 검사
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            
            // ImageIO가 읽지 못하는 파일 (확장자 변조 등)
            if (image == null) {
                addConstraintViolation(context, "올바른 이미지 파일 형식이 아닙니다. (PNG, JPG, JPEG 권장)");
                return false;
            }

            // 해상도 검사 (896 x 200)
            if (image.getWidth() != 896 || image.getHeight() != 200) {
                addConstraintViolation(context, "배너 이미지의 해상도는 896x200 이어야 합니다. (현재: " + image.getWidth() + "x" + image.getHeight() + ")");
                return false;
            }

        } catch (IOException e) {
            addConstraintViolation(context, "이미지 파일을 읽는 중 오류가 발생했습니다.");
            return false;
        }

        return true;
    }

    // 커스텀 에러 메시지를 응답으로 보내기 위한 유틸 메서드
    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage) {
        context.buildConstraintViolationWithTemplate(errorMessage)
               .addConstraintViolation();
    }
}