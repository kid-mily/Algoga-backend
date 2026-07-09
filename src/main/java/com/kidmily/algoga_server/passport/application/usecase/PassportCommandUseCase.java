package com.kidmily.algoga_server.passport.application.usecase;

import com.kidmily.algoga_server.passport.application.command.RegisterPassportCommand;
import com.kidmily.algoga_server.passport.application.command.UpdatePassportCommand;
import com.kidmily.algoga_server.passport.domain.model.Passport;

// 외부 계층(Controller)이 바라보는 여권 등록/수정 인터페이스
public interface PassportCommandUseCase {

    Passport register(RegisterPassportCommand command);

    Passport update(UpdatePassportCommand command);
}
