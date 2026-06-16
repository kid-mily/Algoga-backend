package com.kidmily.algoga_server.packages.application.usecase;

import com.kidmily.algoga_server.packages.application.command.CreatePackageCommand;
import com.kidmily.algoga_server.packages.application.command.UpdatePackageCommand;

public interface PackageCommandUseCase {
    Long create(CreatePackageCommand command);
    void update(Long packageId, UpdatePackageCommand command);
    void delete(Long packageId);
}
