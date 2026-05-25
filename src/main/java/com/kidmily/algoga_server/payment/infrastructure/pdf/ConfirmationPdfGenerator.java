package com.kidmily.algoga_server.payment.infrastructure.pdf;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.kidmily.algoga_server.booking.domain.model.Booking;
import com.kidmily.algoga_server.payment.domain.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ConfirmationPdfGenerator {

    private static final String FONT_PATH = "C:/Windows/Fonts/malgun.ttf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public byte[] generate(Payment payment, Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfEncodings.IDENTITY_H);

            // 제목
            document.add(new Paragraph("여행 예약 확인서")
                    .setFont(font)
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 구분선
            document.add(new Paragraph("─".repeat(60))
                    .setFont(font)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 예약 정보 테이블
            document.add(new Paragraph("■ 예약 정보")
                    .setFont(font)
                    .setFontSize(13)
                    .setBold()
                    .setMarginBottom(8));

            Table bookingTable = createTable(font);
            addRow(bookingTable, font, "예약 번호", booking.getBookingNumber());
            addRow(bookingTable, font, "체크인", booking.getCheckInDate().format(DATE_FORMATTER));
            addRow(bookingTable, font, "체크아웃", booking.getCheckOutDate().format(DATE_FORMATTER));
            addRow(bookingTable, font, "숙박 일수", booking.getNights() + "박");
            addRow(bookingTable, font, "예약 상태", booking.getStatus().name());
            document.add(bookingTable);

            document.add(new Paragraph(" ").setMarginBottom(12));

            // 결제 정보 테이블
            document.add(new Paragraph("■ 결제 정보")
                    .setFont(font)
                    .setFontSize(13)
                    .setBold()
                    .setMarginBottom(8));

            Table paymentTable = createTable(font);
            addRow(paymentTable, font, "결제 유형", payment.getPaymentType().name());
            addRow(paymentTable, font, "결제 금액", String.format("%,d원", payment.getAmount()));
            addRow(paymentTable, font, "마일리지 사용", String.format("%,d원", payment.getUsedMileage()));
            addRow(paymentTable, font, "결제 상태", payment.getStatus().name());
            addRow(paymentTable, font, "결제 일시", payment.getCreatedAt().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            document.add(paymentTable);

            document.add(new Paragraph(" ").setMarginBottom(20));

            // 총 금액
            document.add(new Paragraph(String.format("총 여행 금액: %,d원", booking.getTotalPrice()))
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(30));

            // 안내 문구
            document.add(new Paragraph("본 확인서는 예약 확인 목적으로 발급되었습니다.")
                    .setFont(font)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.warn("[ConfirmationPdfGenerator] PDF 생성 실패 - paymentId: {}", payment.getId(), e);
            throw new RuntimeException("PDF 생성에 실패했습니다.", e);
        }
    }

    private Table createTable(PdfFont font) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private void addRow(Table table, PdfFont font, String key, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(key).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(6));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(10))
                .setPadding(6));
    }
}