package com.kidmily.algoga_server.calendar.application.port;

public interface UserPort {
    String getUserEmail(Long userId);
    String getUserName(Long userId);
}