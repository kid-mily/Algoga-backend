package com.kidmily.algoga_server.user.settings.security.details;

import com.kidmily.algoga_server.user.domain.model.User;
import com.kidmily.algoga_server.user.domain.repository.UserRepository;
import com.kidmily.algoga_server.user.exception.UserErrorCode;
import com.kidmily.algoga_server.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserException(UserErrorCode.NOT_FOUND_USER)
                );

        return new CustomUserDetails(user);
    }
}