package com.kidmily.algoga_server.admin.application.usecase;

import com.kidmily.algoga_server.admin.application.command.CreateManagerCommand;
import com.kidmily.algoga_server.admin.application.command.UpdateManagerCommand;
import com.kidmily.algoga_server.admin.domain.model.Manager;
import java.util.List;

public interface ManagerManageUseCase {
    void createManager(CreateManagerCommand command);
    void updateManager(UpdateManagerCommand command);
    void deleteManager(Long managerId);
    List<Manager> getManagers(String keyword);
}