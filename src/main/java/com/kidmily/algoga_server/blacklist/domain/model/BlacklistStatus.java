package com.kidmily.algoga_server.blacklist.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "블랙리스트 상태 (ACTIVE: 적용 중, INACTIVE: 해제됨)")
public enum BlacklistStatus {
    ACTIVE,
    INACTIVE
}