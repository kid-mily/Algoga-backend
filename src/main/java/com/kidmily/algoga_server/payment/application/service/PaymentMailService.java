package com.kidmily.algoga_server.payment.application.service;

import com.kidmily.algoga_server.payment.domain.event.PaymentCompletedEvent;
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
        String paymentTypeLabel = switch (event.paymentType()) {
            case DEPOSIT -> "계도금";
            case BALANCE -> "잔금";
            case FULL -> "전액";
            case LECTURE_ONLY -> "강의 단독";
        };

        return """
                <html><body style="font-family: sans-serif; color: #333;">
                <div style="max-width:600px; margin:0 auto; padding:30px;">
                  <h2 style="color:#2c7be5;">✈ 알고가 결제 완료 안내</h2>
                  <p>안녕하세요, <strong>%s</strong>님!</p>
                  <p>결제가 정상적으로 완료되었습니다.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:20px;">
                    <tr style="background:#f5f5f5;">
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">예약 번호</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">결제 유형</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background:#f5f5f5;">
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">결제 금액</td>
                      <td style="padding:10px; border:1px solid #ddd;">%,d원</td>
                    </tr>
                    <tr>
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">결제 일시</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:30px; color:#888; font-size:12px;">문의: algoga.official@gmail.com</p>
                </div>
                </body></html>
                """.formatted(
                event.userName(),
                event.bookingNumber(),
                paymentTypeLabel,
                event.amount(),
                event.paidAt().toString().replace("T", " ").substring(0, 16)
        );
    }
}