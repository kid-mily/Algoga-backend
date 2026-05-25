package com.kidmily.algoga_server.admin.application.usecase;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.user.presentation.response.AuthTokenResponse; // 반환 DTO는 유저와 공유

public interface ManagerAuthUseCase {
    AuthTokenResponse login(LoginManagerCommand command);
}