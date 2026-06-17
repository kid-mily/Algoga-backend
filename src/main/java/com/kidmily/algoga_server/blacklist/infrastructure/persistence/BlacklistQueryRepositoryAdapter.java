package com.kidmily.algoga_server.blacklist.infrastructure.persistence;

import com.kidmily.algoga_server.blacklist.domain.repository.BlacklistQueryRepository;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.repository.SpringDataBlacklistQueryRepository;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistUserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BlacklistQueryRepositoryAdapter implements BlacklistQueryRepository {

    private final SpringDataBlacklistQueryRepository springDataRepository;

    @Override
    public Page<BlacklistUserDetailResponse> findCandidateUsers(String keyword, Pageable pageable) {
        return springDataRepository.findAllCandidateUsers(keyword, pageable)
                .map(BlacklistUserDetailResponse::from);
    }

    @Override
    public Page<BlacklistUserDetailResponse> findBlacklistedUsers(String keyword, Pageable pageable) {
        return springDataRepository.findAllBlacklistedUsers(keyword, pageable)
                .map(BlacklistUserDetailResponse::from);
    }

    @Override
    public Optional<BlacklistUserDetailResponse> findUserDetailById(Long userId) {
        return springDataRepository.findUserDetailById(userId)
                .map(BlacklistUserDetailResponse::from);
    }
}