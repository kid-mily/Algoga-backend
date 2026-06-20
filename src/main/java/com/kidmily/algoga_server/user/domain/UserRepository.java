package com.kidmily.algoga_server.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일과 삭제 여부로 유저 조회
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByUsernameAndEmail(String username, String email);

    Optional<User> findByPersonalCode(String personalCode);
    // 개인 코드 중복 확인용
    boolean existsByPersonalCode(String personalCode);

    boolean existsByUsername(String username);

    List<User> findByNicknameContaining(String keyword);
    List<User> findByNameContaining(String keyword);
    @Query("SELECT u.id FROM User u WHERE u.isDeleted = false")
    List<Long> findAllActiveUserIds();

    // 14일 전에 탈퇴한(isDeleted=true) 유저 목록 가져오기
    List<User> findByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);

    // 관리자용: 탈퇴하지 않은 전체 유저 목록을 페이징하여 조회
    Page<User> findByIsDeletedFalse(Pageable pageable);

    // 결과를 담을 아주 얇은 바구니(인터페이스)를 하나 선언합니다.
    interface SignupPathStat {
        String getPath();
        Long getCount();
    }

    // DB에게 "가입경로별로 그룹 묶어서 숫자 세서 줘!" 라고 명령합니다. (탈퇴한 유저는 제외)
    @Query("SELECT u.signupPath AS path, COUNT(u) AS count " +
            "FROM User u " +
            "WHERE u.isDeleted = false " +
            "GROUP BY u.signupPath")
    List<SignupPathStat> countUsersBySignupPath();
}