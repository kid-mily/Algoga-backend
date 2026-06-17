package com.kidmily.algoga_server.blacklist.presentation.api.response;

import com.kidmily.algoga_server.blacklist.domain.model.Blacklist;
import com.kidmily.algoga_server.blacklist.domain.model.BlacklistStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "블랙리스트 단건 기본 응답 DTO")
public record BlacklistResponse(
        @Schema(description = "블랙리스트 내역 고유 ID (PK)", example = "1")
        Long id,

        @Schema(description = "블랙리스트에 제재된 유저 ID", example = "105")
        Long userId,

        @Schema(description = "블랙리스트 등록 상세 사유", example = "운영정책 5회 이상 위반 및 지속적인 악성 스팸 댓글 게시")
        String reason,

        @Schema(description = "현재 블랙리스트 상태 (ACTIVE: 적용 중, INACTIVE: 해제됨)", example = "ACTIVE")
        BlacklistStatus status,

        @Schema(description = "블랙리스트 등록 일시", example = "2026-06-16T15:30:00")
        LocalDateTime createdAt,

        @Schema(description = "블랙리스트 해제 일시 (아직 해제되지 않은 경우 null)", nullable = true, example = "null")
        LocalDateTime unblacklistedAt
) {
    public static BlacklistResponse from(Blacklist domain) {
        if (domain == null) {
            return null;
        }
        return new BlacklistResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getReason(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUnblacklistedAt()
        );
    }
}