package com.kidmily.algoga_server.blacklist.application.usecase;

import com.kidmily.algoga_server.blacklist.application.command.RegisterBlacklistCommand;

public interface BlacklistCommandUseCase {
    void registerBlacklist(RegisterBlacklistCommand command);
    void deregisterBlacklist(Long userId);
}