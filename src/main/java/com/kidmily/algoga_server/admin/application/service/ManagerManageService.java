package com.kidmily.algoga_server.admin.application.service;

import com.kidmily.algoga_server.admin.application.command.CreateManagerCommand;
import com.kidmily.algoga_server.admin.application.command.UpdateManagerCommand;
import com.kidmily.algoga_server.admin.application.usecase.ManagerManageUseCase;
import com.kidmily.algoga_server.admin.domain.model.Manager;
import com.kidmily.algoga_server.admin.domain.model.ManagerRole;
import com.kidmily.algoga_server.admin.domain.repository.ManagerRepository;
import com.kidmily.algoga_server.admin.exception.ManagerErrorCode;
import com.kidmily.algoga_server.admin.exception.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ManagerManageService implements ManagerManageUseCase {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createManager(CreateManagerCommand command) {
        if (managerRepository.existsByLoginId(command.loginId())) {
            throw new ManagerException(ManagerErrorCode.ALREADY_EXISTS_LOGIN_ID);
        }
        Manager manager = Manager.create(
                command.loginId(), passwordEncoder.encode(command.password()),
                command.name(), command.phone(), command.email(),
                ManagerRole.valueOf(command.role().toUpperCase())
        );
        managerRepository.save(manager);
    }

    @Override
    public void updateManager(UpdateManagerCommand command) {
        Manager manager = managerRepository.findById(command.managerId())
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        manager.update(command.role(), command.phone(), command.email());
        managerRepository.save(manager);
    }

    @Override
    public void deleteManager(Long managerId) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

        manager.delete();
        managerRepository.save(manager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Manager> getManagers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return managerRepository.findAll();
        }
        return managerRepository.searchByKeyword(keyword);
    }
}