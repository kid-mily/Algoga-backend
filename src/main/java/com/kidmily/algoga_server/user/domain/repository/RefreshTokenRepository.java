package com.kidmily.algoga_server.user.domain.repository;

import com.kidmily.algoga_server.example.domain.model.Example;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트(Port)
public interface RefreshTokenRepository {
    Example save(Example example);
    Optional<Example> findById(Long id);
}