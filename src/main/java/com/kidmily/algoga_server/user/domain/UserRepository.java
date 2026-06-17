package com.kidmily.algoga_server.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일과 삭제 여부로 유저 조회
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByUsernameAndEmail(String username, String email);

    Optional<User> findByPersonalCode(String personalCode);

    boolean existsByUsername(String username);

    List<User> findByNicknameContaining(String keyword);
    List<User> findByNameContaining(String keyword);
    @Query("SELECT u.id FROM User u WHERE u.isDeleted = false")
    List<Long> findAllActiveUserIds();

    // 14일 전에 탈퇴한(isDeleted=true) 유저 목록 가져오기
    List<User> findByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);
}