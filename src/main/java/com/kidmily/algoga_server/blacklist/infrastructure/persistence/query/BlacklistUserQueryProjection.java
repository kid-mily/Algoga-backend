package com.kidmily.algoga_server.blacklist.infrastructure.persistence.query;

import java.time.LocalDateTime;

public interface BlacklistUserQueryProjection {
    Long getUserId();
    String getUsername();
    String getName();
    String getNickname();
    String getEmail();
    Long getReportCount();
    LocalDateTime getLastReportedAt();
    Integer getIsBlacklisted();
}