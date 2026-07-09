package com.kidmily.algoga_server.passport.infrastructure.persistence;

import com.kidmily.algoga_server.passport.domain.model.Passport;
import com.kidmily.algoga_server.passport.domain.repository.PassportRepository;
import com.kidmily.algoga_server.passport.infrastructure.mapper.PassportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PassportRepositoryAdapter implements PassportRepository {

    private final SpringDataPassportRepository springDataRepository;
    private final PassportMapper passportMapper;

    @Override
    public Passport save(Passport passport) {
        PassportJpaEntity entity = passportMapper.toJpaEntity(passport);
        PassportJpaEntity savedEntity = springDataRepository.save(entity);
        return passportMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Passport> findByUserId(Long userId) {
        return springDataRepository.findByUserId(userId)
                .map(passportMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return springDataRepository.existsByUserId(userId);
    }
}
