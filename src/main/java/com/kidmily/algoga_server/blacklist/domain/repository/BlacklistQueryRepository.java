package com.kidmily.algoga_server.blacklist.domain.repository;

import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistUserDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BlacklistQueryRepository {
    Page<BlacklistUserDetailResponse> findCandidateUsers(String keyword, Pageable pageable);
    Page<BlacklistUserDetailResponse> findBlacklistedUsers(String keyword, Pageable pageable);
    Optional<BlacklistUserDetailResponse> findUserDetailById(Long userId);
}