package com.kidmily.algoga_server.admin.application.usecase;

import com.kidmily.algoga_server.admin.application.command.LoginManagerCommand;
import com.kidmily.algoga_server.admin.presentation.api.response.AdminAuthTokenResponse;
public interface ManagerAuthUseCase {
    AdminAuthTokenResponse login(LoginManagerCommand command);
}