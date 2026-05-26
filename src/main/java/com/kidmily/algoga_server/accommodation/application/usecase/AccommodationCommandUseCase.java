package com.kidmily.algoga_server.accommodation.application.usecase;

import com.kidmily.algoga_server.accommodation.application.command.CreateAccommodationCommand;
import com.kidmily.algoga_server.accommodation.application.command.UpdateAccommodationCommand;

public interface AccommodationCommandUseCase {
    Long create(CreateAccommodationCommand command);
    void update(Long accommodationId, UpdateAccommodationCommand command);
    void delete(Long accommodationId);
}