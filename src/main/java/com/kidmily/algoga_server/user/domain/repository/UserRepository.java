package com.kidmily.algoga_server.user.domain.repository;

import com.kidmily.algoga_server.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}