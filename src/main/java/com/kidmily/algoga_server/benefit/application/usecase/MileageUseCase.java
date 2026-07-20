package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.AdminMileageTransactionCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardReferralSignupCommand;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageHistoryResult;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageSummaryResult;

import java.util.List;

public interface MileageUseCase {

    AdminMileageSummaryResult getMileageUsers();

    List<AdminMileageHistoryResult> getUserMileageHistories(Long userId);

    AdminMileageHistoryResult earnMileage(AdminMileageTransactionCommand command);

    AdminMileageHistoryResult useMileage(AdminMileageTransactionCommand command);

    void rewardReferralSignup(RewardReferralSignupCommand command);
}
