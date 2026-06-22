package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.IssueWelcomeCouponCommand;

public interface WelcomeCouponUseCase {

    void issueWelcomeCoupon(IssueWelcomeCouponCommand command);
}