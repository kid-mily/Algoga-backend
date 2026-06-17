package com.kidmily.algoga_server.blacklist.infrastructure.persistence;

import com.kidmily.algoga_server.blacklist.domain.model.Blacklist;
import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;
import com.kidmily.algoga_server.blacklist.domain.repository.BlacklistRepository;
import com.kidmily.algoga_server.blacklist.infrastructure.mapper.BlacklistMapper;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.entity.BlacklistJpaEntity;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.repository.SpringDataBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BlacklistRepositoryAdapter implements BlacklistRepository {

    private final SpringDataBlacklistRepository springDataRepository;
    private final BlacklistMapper mapper;

    @Override
    public Blacklist save(Blacklist blacklist) {
        BlacklistJpaEntity entity = mapper.toJpaEntity(blacklist);
        BlacklistJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByUserIdAndStatus(Long userId, BlacklistStatus status) {
        return springDataRepository.existsByUserIdAndStatus(userId, status);
    }

    @Override
    public Optional<Blacklist> findByUserIdAndStatus(Long userId, BlacklistStatus status) {
        return springDataRepository.findByUserIdAndStatus(userId, status)
                .map(mapper::toDomain);
    }
}