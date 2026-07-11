package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.refund.domain.event.RefundApprovedEvent;
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
public class RefundMailService {

    private final JavaMailSender mailSender;

    @Async
    @EventListener
    public void handleRefundApproved(RefundApprovedEvent event) {
        log.info("[RefundMailService] 환불 완료 메일 발송 시작 - userId: {}, email: {}",
                event.userId(), event.userEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.userEmail());
            helper.setSubject("[알고가] 환불이 완료되었습니다");
            helper.setText(buildEmailBody(event), true);

            mailSender.send(message);
            log.info("[RefundMailService] 환불 완료 메일 발송 성공 - userId: {}", event.userId());
        } catch (MessagingException e) {
            log.warn("[RefundMailService] 환불 완료 메일 발송 실패 - userId: {}, error: {}",
                    event.userId(), e.getMessage());
        }
    }

    private String buildEmailBody(RefundApprovedEvent event) {
        String typeLabel = "LECTURE".equals(event.type()) ? "강의" : "여행 패키지";

        return """
                <html><body style="font-family: sans-serif; color: #333;">
                <div style="max-width:600px; margin:0 auto; padding:30px;">
                  <h2 style="color:#2c7be5;">✈ 알고가 환불 완료 안내</h2>
                  <p>안녕하세요, <strong>%s</strong>님!</p>
                  <p>요청하신 환불이 정상적으로 완료되었습니다.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:20px;">
                    <tr style="background:#f5f5f5;">
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">예약 번호</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">환불 유형</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background:#f5f5f5;">
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">환불 금액</td>
                      <td style="padding:10px; border:1px solid #ddd;">%,d원</td>
                    </tr>
                    <tr>
                      <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">환불 일시</td>
                      <td style="padding:10px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:30px; color:#888; font-size:12px;">문의: algoga.official@gmail.com</p>
                </div>
                </body></html>
                """.formatted(
                event.userName(),
                event.bookingNumber(),
                typeLabel,
                event.refundAmount(),
                event.refundedAt().toString().replace("T", " ").substring(0, 16)
        );
    }
}