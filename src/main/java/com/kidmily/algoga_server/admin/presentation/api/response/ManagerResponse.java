package com.kidmily.algoga_server.admin.presentation.api.response;

import com.kidmily.algoga_server.admin.domain.model.Manager;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "매니저 정보 응답")
public record ManagerResponse(
        @Schema(description = "매니저 PK", example = "1") Long id,
        @Schema(description = "로그인 아이디", example = "admin_01") String loginId,
        @Schema(description = "이름", example = "홍길동 관리자") String name,
        @Schema(description = "전화번호", example = "010-1234-5678") String phone,
        @Schema(description = "이메일", example = "admin@algoga.com") String email,
        @Schema(description = "권한", example = "CS_MANAGER") String role,
        @Schema(description = "삭제 여부", example = "false") boolean isDeleted,
        @Schema(description = "생성 일자") LocalDateTime createdAt
) {
    // 🌟 도메인 객체를 DTO로 변환해주는 팩토리 메서드
    public static ManagerResponse from(Manager manager) {
        return new ManagerResponse(
                manager.getId(), manager.getLoginId(), manager.getName(),
                manager.getPhone(), manager.getEmail(), manager.getRole().name(),
                manager.isDeleted(), manager.getCreatedAt()
        );
    }
}