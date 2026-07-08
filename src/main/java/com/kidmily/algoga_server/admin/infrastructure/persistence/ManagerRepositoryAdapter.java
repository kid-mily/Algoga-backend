package com.kidmily.algoga_server.admin.infrastructure.persistence;

import com.kidmily.algoga_server.admin.domain.model.Manager;
import com.kidmily.algoga_server.admin.domain.repository.ManagerRepository;
import com.kidmily.algoga_server.admin.infrastructure.mapper.ManagerMapper;
import com.kidmily.algoga_server.admin.infrastructure.persistence.entity.ManagerJpaEntity;
import com.kidmily.algoga_server.admin.infrastructure.persistence.repository.SpringDataManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ManagerRepositoryAdapter implements ManagerRepository {

    // super_admin의 pk (전체 조회 및 검색에서 제외)
    private static final Long SUPER_ADMIN_ID = 1L;

    private final SpringDataManagerRepository jpaRepository;
    private final ManagerMapper managerMapper; // 🌟 MapStruct 매퍼 주입

    @Override
    public Manager save(Manager manager) {
        // MapStruct를 통한 Domain -> JPA Entity 매핑
        ManagerJpaEntity entity = managerMapper.toJpaEntity(manager);

        // DB 저장
        ManagerJpaEntity saved = jpaRepository.save(entity);

        // MapStruct를 통한 JPA Entity -> Domain 매핑
        return managerMapper.toDomain(saved);
    }

    @Override
    public Optional<Manager> findByLoginId(String loginId) {
        // DB 조회 후 MapStruct 메서드 참조를 활용하여 Domain 반환
        return jpaRepository.findByLoginId(loginId)
                .map(managerMapper::toDomain);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return jpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Optional<Manager> findById(Long id) {
        return jpaRepository.findById(id).map(managerMapper::toDomain);
    }

    @Override
    public List<Manager> findAll() {
        return jpaRepository.findAllExcludingId(SUPER_ADMIN_ID).stream()
                .map(managerMapper::toDomain).toList();
    }

    @Override
    public List<Manager> searchByKeyword(String keyword) {
        return jpaRepository.searchByKeywordExcludingId(keyword, SUPER_ADMIN_ID)
                .stream().map(managerMapper::toDomain).toList();
    }
}