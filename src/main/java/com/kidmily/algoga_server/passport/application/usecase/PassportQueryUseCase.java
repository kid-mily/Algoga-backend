package com.kidmily.algoga_server.passport.application.usecase;

import com.kidmily.algoga_server.passport.domain.model.Passport;

// 외부 계층(Controller)이 바라보는 여권 조회 인터페이스
public interface PassportQueryUseCase {

    Passport getMyPassport(Long userId);
}
