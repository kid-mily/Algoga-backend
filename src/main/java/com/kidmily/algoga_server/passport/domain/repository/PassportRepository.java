package com.kidmily.algoga_server.passport.domain.repository;

import com.kidmily.algoga_server.passport.domain.model.Passport;

import java.util.Optional;

// Application, Domain 계층이 사용할 여권 레포지토리 포트(Port)
public interface PassportRepository {

    Passport save(Passport passport);

    Optional<Passport> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
