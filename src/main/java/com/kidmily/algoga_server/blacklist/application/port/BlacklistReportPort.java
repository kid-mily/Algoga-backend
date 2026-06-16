package com.kidmily.algoga_server.blacklist.application.port;

public interface BlacklistReportPort {
    long getCompletedReportCount(Long userId);
}