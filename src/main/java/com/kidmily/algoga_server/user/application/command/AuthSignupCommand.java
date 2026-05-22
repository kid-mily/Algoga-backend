package com.kidmily.algoga_server.user.application.command;

import com.kidmily.algoga_server.user.domain.model.Gender;
import java.time.LocalDate;

public record AuthSignupCommand(
        String email,
        String password,
        String name,
        String phone,
        LocalDate birthDate,
        Gender gender,
        String nickname
) {}