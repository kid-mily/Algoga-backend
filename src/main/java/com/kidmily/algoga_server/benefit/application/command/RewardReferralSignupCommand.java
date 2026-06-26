package com.kidmily.algoga_server.benefit.application.command;

public record RewardReferralSignupCommand(
        Long referredUserId,
        Long referrerUserId,
        String referralCode
) {
}
