package com.kidmily.algoga_server.user.application;

public interface EmailSender {
    void sendEmail(String toAddress, String subject, String body);
}