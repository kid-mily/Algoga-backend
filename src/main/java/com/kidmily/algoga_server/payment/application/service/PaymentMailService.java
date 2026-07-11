package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
import com.kidmily.algoga_server.payment.domain.model.PaymentType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMailService {

    private final JavaMailSender mailSender;

    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[PaymentMailService] 결제 완료 메일 발송 시작 - userId: {}, email: {}",
                event.userId(), event.userEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.userEmail());
            helper.setSubject("[알고가] 결제가 완료되었습니다");
            helper.setText(buildEmailBody(event), true);

            mailSender.send(message);
            log.info("[PaymentMailService] 결제 완료 메일 발송 성공 - userId: {}", event.userId());
        } catch (MessagingException e) {
            log.warn("[PaymentMailService] 결제 완료 메일 발송 실패 - userId: {}, error: {}",
                    event.userId(), e.getMessage());
        }
    }

    private String buildEmailBody(PaymentCompletedEvent event) {
        boolean isLecture = event.paymentType() == PaymentType.LECTURE_ONLY;

        String paymentTypeLabel = switch (event.paymentType()) {
            case DEPOSIT -> "계도금";
            case BALANCE -> "잔금";
            case FULL -> "전액";
            case LECTURE_ONLY -> "강의 단독";
        };

        String infoLabel = isLecture ? "강의명" : "예약 번호";
        String infoValue = isLecture
                ? (event.productName() != null ? event.productName() : "강의")
                : event.bookingNumber();

        // 표 행을 동적으로 구성 (zebra 배경은 shaded 플래그로 번갈아)
        StringBuilder rows = new StringBuilder();
        rows.append(row(infoLabel, infoValue, true));
        rows.append(row("결제 유형", paymentTypeLabel, false));
        rows.append(row("결제 금액", String.format("%,d원", event.amount()), true));
        rows.append(row("결제 일시", formatDateTime(event.paidAt()), false));

        // 패키지 결제일 때만 여행 상세 추가
        if (!isLecture) {
            if (event.accommodationName() != null)
                rows.append(row("숙소 이름", event.accommodationName(), true));
            if (event.accommodationAddress() != null)
                rows.append(row("숙소 주소", event.accommodationAddress(), false));
            if (event.checkInDate() != null)
                rows.append(row("체크인", event.checkInDate().toString(), true));
            if (event.checkOutDate() != null)
                rows.append(row("체크아웃", event.checkOutDate().toString(), false));
            if (event.airline() != null || event.flightNumber() != null)
                rows.append(row("항공편", (nvl(event.airline()) + " " + nvl(event.flightNumber())).trim(), true));
            if (event.departureTime() != null)
                rows.append(row("출발", formatDateTime(event.departureTime()), false));
            if (event.arrivalTime() != null)
                rows.append(row("도착", formatDateTime(event.arrivalTime()), true));
        }

        return """
            <html><body style="font-family: sans-serif; color: #333;">
            <div style="max-width:600px; margin:0 auto; padding:30px;">
              <h2 style="color:#2c7be5;">✈ 알고가 결제 완료 안내</h2>
              <p>안녕하세요, <strong>%s</strong>님!</p>
              <p>결제가 정상적으로 완료되었습니다.</p>
              <table style="width:100%%; border-collapse:collapse; margin-top:20px;">
                %s
              </table>
              <p style="margin-top:30px; color:#888; font-size:12px;">문의: algoga.official@gmail.com</p>
            </div>
            </body></html>
            """.formatted(event.userName(), rows.toString());
    }

    // 표 한 행 생성 헬퍼
    private String row(String label, String value, boolean shaded) {
        String bg = shaded ? " style=\"background:#f5f5f5;\"" : "";
        return """
            <tr%s>
              <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">%s</td>
              <td style="padding:10px; border:1px solid #ddd;">%s</td>
            </tr>
            """.formatted(bg, label, value);
    }

    private String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        String s = dt.toString().replace("T", " ");
        return s.length() >= 16 ? s.substring(0, 16) : s;   // yyyy-MM-dd HH:mm
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}