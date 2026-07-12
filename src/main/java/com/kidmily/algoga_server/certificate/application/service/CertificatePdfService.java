package com.kidmily.algoga_server.certificate.application.service;

import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.completion.domain.model.CourseCompletion;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificatePdfService {

    private static final String CLASSPATH_KOREAN_FONT = "fonts/NotoSansKR-VF.ttf";

    private static final List<String> SYSTEM_KOREAN_FONT_PATHS = List.of(
            "C:/Windows/Fonts/malgun.ttf",
            "C:/Windows/Fonts/gulim.ttf",
            "/System/Library/Fonts/AppleSDGothicNeo.ttc",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/noto/NotoSansKR-Regular.ttf",
            "/usr/share/fonts/opentype/noto/NotoSansKR-Regular.otf"
    );

    private static final Color TEAL = new Color(96, 154, 151);
    private static final Color DARK = new Color(20, 28, 45);
    private static final Color GRAY = new Color(120, 135, 160);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color PALE_TEAL = new Color(232, 244, 242);
    private static final Color PALE_BLUE = new Color(241, 245, 249);

    private final CourseRepository courseRepository;
    private final CourseCompletionRepository courseCompletionRepository;

    public byte[] generateCertificatePdf(Long userId, String userName, Long courseId) {
        log.info("[Certificate Query] 수료증 PDF 발급 요청. userId={}, courseId={}", userId, courseId);

        CourseCompletion courseCompletion = courseCompletionRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> {
                    log.warn("[Certificate Query] 수료증 발급 실패. 강의 이수 내역이 없습니다. userId={}, courseId={}",
                            userId, courseId);
                    return new LmsException(LmsErrorCode.COURSE_COMPLETION_NOT_FOUND);
                });

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.warn("[Certificate Query] 수료증 발급 실패. 존재하지 않는 강의입니다. courseId={}", courseId);
                    return new LmsException(LmsErrorCode.COURSE_NOT_FOUND);
                });

        try {
            byte[] pdfBytes = createPdf(resolveUserName(userName), course, courseCompletion);

            log.info("[Certificate Query] 수료증 PDF 발급 완료. userId={}, courseId={}, certificateCode={}",
                    userId, courseId, courseCompletion.getCertificateCode());

            return pdfBytes;
        } catch (IOException exception) {
            log.error("[Certificate Query] 수료증 PDF 생성 실패. userId={}, courseId={}", userId, courseId, exception);
            throw new IllegalStateException("수료증 PDF 생성에 실패했습니다.", exception);
        }
    }

    private byte[] createPdf(String userName, Course course, CourseCompletion courseCompletion) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = loadKoreanFont(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawCertificate(contentStream, font, userName, course, courseCompletion);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void drawCertificate(
            PDPageContentStream contentStream,
            PDType0Font font,
            String userName,
            Course course,
            CourseCompletion courseCompletion
    ) throws IOException {
        String completedDate = courseCompletion.getCompletedAt()
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        float pageWidth = PDRectangle.A4.getWidth();

        fillRect(contentStream, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight(), LIGHT_BG);

        float cardX = 55;
        float cardY = 70;
        float cardWidth = 485;
        float cardHeight = 705;

        drawRoundedRect(contentStream, cardX, cardY, cardWidth, cardHeight, 12, Color.WHITE, TEAL, 2.2f);

        drawCircle(contentStream, pageWidth / 2, 710, 34, PALE_BLUE, null, 0);
        drawRibbonIcon(contentStream, pageWidth / 2, 710);

        drawCenteredText(contentStream, font, 28, "수료증", 650, TEAL);
        drawCenteredText(contentStream, font, 16, "Certificate of Completion", 615, DARK);

        drawLine(contentStream, 244, 585, 351, 585, TEAL, 2.5f);

        drawCenteredText(contentStream, font, 12, "본 수료증은 아래와 같이 증명합니다.", 545, GRAY);
        drawCenteredText(contentStream, font, 20, userName, 505, DARK);

        drawCenteredText(contentStream, font, 11, "위 수료자는 알고가(ALGOGA)에서 제공하는", 462, DARK);
        drawCenteredTextWithMaxWidth(contentStream, font, 17, "'" + safeText(course.getTitle()) + "'", 432, TEAL, 380);

        drawCenteredText(contentStream, font, 11, "과정을 성실히 이수하였으며,", 395, DARK);
        drawCenteredText(contentStream, font, 11, "수료 기준을 충족하였음을 증명합니다.", 373, DARK);

        drawCenteredText(contentStream, font, 13, completedDate, 325, DARK);

        drawRoundedRect(contentStream, 193, 260, 210, 52, 10, PALE_BLUE, null, 0);
        drawCenteredText(contentStream, font, 8, "인증번호", 294, GRAY);
        drawCenteredText(contentStream, font, 10, courseCompletion.getCertificateCode(), 275, DARK);

        drawCircle(contentStream, pageWidth / 2, 180, 34, PALE_TEAL, null, 0);
        drawCenteredText(contentStream, font, 8, "ALGOGA", 177, TEAL);

        drawLine(contentStream, 105, 130, 490, 130, PALE_TEAL, 1.2f);
        drawCenteredText(contentStream, font, 8, "This certificate is issued by Algoga Learning Management System.", 105, GRAY);
    }

    private void drawRibbonIcon(PDPageContentStream contentStream, float centerX, float centerY) throws IOException {
        contentStream.setStrokingColor(TEAL);
        contentStream.setLineWidth(2.4f);

        contentStream.addRect(centerX - 8, centerY - 3, 16, 16);
        contentStream.stroke();

        contentStream.moveTo(centerX - 6, centerY - 3);
        contentStream.lineTo(centerX - 11, centerY - 20);
        contentStream.lineTo(centerX - 2, centerY - 14);
        contentStream.stroke();

        contentStream.moveTo(centerX + 6, centerY - 3);
        contentStream.lineTo(centerX + 11, centerY - 20);
        contentStream.lineTo(centerX + 2, centerY - 14);
        contentStream.stroke();
    }

    private void drawText(
            PDPageContentStream contentStream,
            PDType0Font font,
            int fontSize,
            String text,
            float x,
            float y,
            Color color
    ) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(color);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(safeText(text));
        contentStream.endText();
    }

    private void drawCenteredText(
            PDPageContentStream contentStream,
            PDType0Font font,
            int fontSize,
            String text,
            float y,
            Color color
    ) throws IOException {
        String safe = safeText(text);
        float pageWidth = PDRectangle.A4.getWidth();
        float textWidth = font.getStringWidth(safe) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;

        drawText(contentStream, font, fontSize, safe, x, y, color);
    }

    private void drawCenteredTextWithMaxWidth(
            PDPageContentStream contentStream,
            PDType0Font font,
            int fontSize,
            String text,
            float y,
            Color color,
            float maxWidth
    ) throws IOException {
        String safe = safeText(text);
        int adjustedFontSize = fontSize;

        while (adjustedFontSize > 9 && font.getStringWidth(safe) / 1000 * adjustedFontSize > maxWidth) {
            adjustedFontSize--;
        }

        drawCenteredText(contentStream, font, adjustedFontSize, safe, y, color);
    }

    private void drawLine(
            PDPageContentStream contentStream,
            float startX,
            float startY,
            float endX,
            float endY,
            Color color,
            float width
    ) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.setLineWidth(width);
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(endX, endY);
        contentStream.stroke();
    }

    private void fillRect(
            PDPageContentStream contentStream,
            float x,
            float y,
            float width,
            float height,
            Color color
    ) throws IOException {
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();
    }

    private void drawRoundedRect(
            PDPageContentStream contentStream,
            float x,
            float y,
            float width,
            float height,
            float radius,
            Color fillColor,
            Color strokeColor,
            float strokeWidth
    ) throws IOException {
        if (fillColor != null) {
            contentStream.setNonStrokingColor(fillColor);
            roundedRectPath(contentStream, x, y, width, height, radius);
            contentStream.fill();
        }

        if (strokeColor != null && strokeWidth > 0) {
            contentStream.setStrokingColor(strokeColor);
            contentStream.setLineWidth(strokeWidth);
            roundedRectPath(contentStream, x, y, width, height, radius);
            contentStream.stroke();
        }
    }

    private void roundedRectPath(
            PDPageContentStream contentStream,
            float x,
            float y,
            float width,
            float height,
            float radius
    ) throws IOException {
        float k = 0.552284749831f;
        float c = radius * k;

        contentStream.moveTo(x + radius, y);
        contentStream.lineTo(x + width - radius, y);
        contentStream.curveTo(x + width - radius + c, y, x + width, y + radius - c, x + width, y + radius);
        contentStream.lineTo(x + width, y + height - radius);
        contentStream.curveTo(x + width, y + height - radius + c, x + width - radius + c, y + height, x + width - radius, y + height);
        contentStream.lineTo(x + radius, y + height);
        contentStream.curveTo(x + radius - c, y + height, x, y + height - radius + c, x, y + height - radius);
        contentStream.lineTo(x, y + radius);
        contentStream.curveTo(x, y + radius - c, x + radius - c, y, x + radius, y);
        contentStream.closePath();
    }

    private void drawCircle(
            PDPageContentStream contentStream,
            float centerX,
            float centerY,
            float radius,
            Color fillColor,
            Color strokeColor,
            float strokeWidth
    ) throws IOException {
        float k = 0.552284749831f;
        float c = radius * k;

        if (fillColor != null) {
            contentStream.setNonStrokingColor(fillColor);
            circlePath(contentStream, centerX, centerY, radius, c);
            contentStream.fill();
        }

        if (strokeColor != null && strokeWidth > 0) {
            contentStream.setStrokingColor(strokeColor);
            contentStream.setLineWidth(strokeWidth);
            circlePath(contentStream, centerX, centerY, radius, c);
            contentStream.stroke();
        }
    }

    private void circlePath(
            PDPageContentStream contentStream,
            float x,
            float y,
            float radius,
            float c
    ) throws IOException {
        contentStream.moveTo(x + radius, y);
        contentStream.curveTo(x + radius, y + c, x + c, y + radius, x, y + radius);
        contentStream.curveTo(x - c, y + radius, x - radius, y + c, x - radius, y);
        contentStream.curveTo(x - radius, y - c, x - c, y - radius, x, y - radius);
        contentStream.curveTo(x + c, y - radius, x + radius, y - c, x + radius, y);
        contentStream.closePath();
    }

    private String resolveUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return "수강생";
        }

        return userName;
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private PDType0Font loadKoreanFont(PDDocument document) throws IOException {
        ClassPathResource fontResource = new ClassPathResource(CLASSPATH_KOREAN_FONT);

        if (fontResource.exists()) {
            try (InputStream inputStream = fontResource.getInputStream()) {
                return PDType0Font.load(document, inputStream);
            }
        }

        for (String fontPath : SYSTEM_KOREAN_FONT_PATHS) {
            File fontFile = new File(fontPath);
            if (fontFile.exists()) {
                return PDType0Font.load(document, fontFile);
            }
        }

        throw new IOException("한글 폰트를 찾을 수 없습니다. classpath=" + CLASSPATH_KOREAN_FONT);
    }
}