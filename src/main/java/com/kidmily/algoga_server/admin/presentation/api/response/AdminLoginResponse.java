package com.kidmily.algoga_server.admin.presentation.api.response;

import com.kidmily.algoga_server.admin.domain.model.ManagerRole;

public record AdminLoginResponse(
        ManagerRole role
) {
}