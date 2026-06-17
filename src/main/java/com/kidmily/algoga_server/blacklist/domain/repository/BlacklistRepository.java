package com.kidmily.algoga_server.blacklist.domain.repository;

import com.kidmily.algoga_server.blacklist.domain.model.Blacklist;
import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;

import java.util.Optional;

public interface BlacklistRepository {
    Blacklist save(Blacklist blacklist);
    boolean existsByUserIdAndStatus(Long userId, BlacklistStatus status);
    Optional<Blacklist> findByUserIdAndStatus(Long userId, BlacklistStatus status);
}