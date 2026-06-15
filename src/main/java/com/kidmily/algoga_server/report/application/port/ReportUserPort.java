package com.kidmily.algoga_server.report.application.port;

import java.util.List;

public interface ReportUserPort {
    String getNickname(Long userId);
    List<Long> findUserIdsByNicknameContaining(String keyword);
}