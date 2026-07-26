package com.kidmily.algoga_server.benefit.application.usecase;

import com.kidmily.algoga_server.benefit.application.command.AdminMileageTransactionCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardReferralSignupCommand;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageHistoryResult;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageSummaryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MileageUseCase {

    AdminMileageSummaryResult getMileageUsers(Pageable pageable);

    Page<AdminMileageHistoryResult> getUserMileageHistories(Long userId, Pageable pageable);

    AdminMileageHistoryResult earnMileage(AdminMileageTransactionCommand command);

    AdminMileageHistoryResult useMileage(AdminMileageTransactionCommand command);

    void rewardReferralSignup(RewardReferralSignupCommand command);
}
