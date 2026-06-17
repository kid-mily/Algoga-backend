package com.kidmily.algoga_server.notice.application.port;

import java.util.List;

// notice/application/port/UserPort.java
public interface UserPort {
    List<Long> findAllActiveUserIds();
}