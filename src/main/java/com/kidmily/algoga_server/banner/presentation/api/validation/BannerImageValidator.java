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
        if (file == null || file.isEmpty()) {
            return true; 
        }

        context.disableDefaultConstraintViolation();

        // 1. 용량 검사
        if (file.getSize() >= MAX_FILE_SIZE) {
            addConstraintViolation(context, "배너 이미지의 크기는 5MB 미만이어야 합니다.");
            return false;
        }

        // 2. 비율 및 형식 검사
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            
            if (image == null) {
                addConstraintViolation(context, "올바른 이미지 파일 형식이 아닙니다. (PNG, JPG, JPEG 권장)");
                return false;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // 7:1 ~ 10:1 비율 검사
            double ratio = (double) width / height;
            if (ratio < 7.0 || ratio > 10.0) {
                addConstraintViolation(context, String.format(
                        "배너 이미지의 가로 비율은 세로 대비 7~10배여야 합니다. (현재 비율: %.1f:1, 해상도: %dx%d)", 
                        ratio, width, height
                ));
                return false;
            }

        } catch (IOException e) {
            addConstraintViolation(context, "이미지 파일을 읽는 중 오류가 발생했습니다.");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage) {
        context.buildConstraintViolationWithTemplate(errorMessage)
               .addConstraintViolation();
    }
}