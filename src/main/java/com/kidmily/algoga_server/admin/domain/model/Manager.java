package com.kidmily.algoga_server.admin.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manager {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String email;
    private ManagerRole role;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 슈퍼 어드민이 새로운 매니저를 생성할 때 사용하는 팩토리 메서드
    public static Manager create(String loginId, String password, String name, String phone, String email, ManagerRole role) {
        Manager manager = new Manager();
        manager.loginId = loginId;
        manager.password = password;
        manager.name = name;
        manager.phone = phone;
        manager.email = email;
        manager.role = role;
        manager.isDeleted = false;
        manager.createdAt = LocalDateTime.now();
        manager.updatedAt = LocalDateTime.now();
        return manager;
    }

    // DB(인프라)에서 조회한 데이터를 도메인 객체로 복원할 때 사용
    public static Manager reconstitute(Long id, String loginId, String password, String name, String phone, String email, ManagerRole role, boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Manager manager = new Manager();
        manager.id = id;
        manager.loginId = loginId;
        manager.password = password;
        manager.name = name;
        manager.phone = phone;
        manager.email = email;
        manager.role = role;
        manager.isDeleted = isDeleted;
        manager.createdAt = createdAt;
        manager.updatedAt = updatedAt;
        return manager;
    }

    // 🌟 매니저 정보 수정 로직
    public void update(String role, String phone, String email) {
        this.role = ManagerRole.valueOf(role.toUpperCase());
        this.phone = phone;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    // 🌟 소프트 딜리트(삭제) 로직
    public void delete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }
}