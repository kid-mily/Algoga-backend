package com.kidmily.algoga_server.global.infrastructure.mail;

public interface EmailSender {
    // 기능 정의만 합니다!
    void sendEmail(String toAddress, String subject, String body);
}