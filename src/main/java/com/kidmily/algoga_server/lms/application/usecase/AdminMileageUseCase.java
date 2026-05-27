package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.AdminMileageTransactionCommand;
import com.kidmily.algoga_server.lms.application.result.AdminMileageHistoryResult;
import com.kidmily.algoga_server.lms.application.result.AdminMileageSummaryResult;

import java.util.List;

public interface AdminMileageUseCase {

    AdminMileageSummaryResult getMileageUsers();

    List<AdminMileageHistoryResult> getUserMileageHistories(Long userId);

    AdminMileageHistoryResult earnMileage(AdminMileageTransactionCommand command);

    AdminMileageHistoryResult useMileage(AdminMileageTransactionCommand command);
}