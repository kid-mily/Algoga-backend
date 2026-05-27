package com.kidmily.algoga_server.admin.domain.repository;

import com.kidmily.algoga_server.admin.domain.model.Manager;
import java.util.List;
import java.util.Optional;

public interface ManagerRepository {
    Manager save(Manager manager);
    Optional<Manager> findById(Long id); // 추가
    Optional<Manager> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);

    // 🌟 전체 조회 및 검색 추가
    List<Manager> findAll();
    List<Manager> searchByKeyword(String keyword);
}