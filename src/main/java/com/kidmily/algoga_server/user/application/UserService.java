package com.kidmily.algoga_server.user.application;

import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 1. 회원 탈퇴 (Soft Delete)
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        user.withdraw(); // User 엔티티의 isDeleted 상태를 true로 변경

        // @Transactional 안에서 엔티티 값이 변경되면 JPA가 알아서 DB에 UPDATE 쿼리를 날립니다! (더티 체킹)
        log.info("회원 탈퇴 처리 완료 [이메일: {}]", email);
    }
}