package com.kidmily.algoga_server.blacklist.application.usecase;

import com.kidmily.algoga_server.blacklist.application.command.DeregisterBlacklistCommand;
import com.kidmily.algoga_server.blacklist.application.command.RegisterBlacklistCommand;

public interface BlacklistCommandUseCase {
    void registerBlacklist(RegisterBlacklistCommand command);
    
    // 🌟 수정됨: Long userId 대신 Command 객체를 받음
    void deregisterBlacklist(DeregisterBlacklistCommand command);
}