package com.kidmily.algoga_server.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByNickname(String nickname);

    // 기존 코드에 아래 한 줄을 추가하세요.
    Optional<User> findByNameAndEmail(String name, String email);

    Optional<User> findByUsernameAndEmail(String username, String email);
}