package com.kidmily.algoga_server.global.websocket;

import java.util.Optional;

public interface WebSocketUserPort {
    Optional<Long> findUserIdByEmail(String email);
}