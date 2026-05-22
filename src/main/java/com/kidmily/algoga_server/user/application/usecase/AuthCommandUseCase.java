package com.kidmily.algoga_server.user.application.usecase;

import com.kidmily.algoga_server.user.application.command.AuthLoginCommand;
import com.kidmily.algoga_server.user.application.command.AuthSignupCommand;
import com.kidmily.algoga_server.user.presentation.api.response.AuthTokenResponse;

public interface AuthCommandUseCase {

    void signup(AuthSignupCommand command);

    AuthTokenResponse login(AuthLoginCommand command);

}