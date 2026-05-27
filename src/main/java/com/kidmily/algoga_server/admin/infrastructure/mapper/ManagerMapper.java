package com.kidmily.algoga_server.admin.infrastructure.mapper;

import com.kidmily.algoga_server.admin.domain.model.Manager;
import com.kidmily.algoga_server.admin.infrastructure.persistence.entity.ManagerJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ManagerMapper {

    // 1. Domain Entity -> JPA Entity 변환
    // ManagerJpaEntity의 필드 이름이 같으므로 MapStruct가 자동으로 매핑합니다.
    ManagerJpaEntity toJpaEntity(Manager manager);

    // 2. JPA Entity -> Domain Entity 변환
    // 도메인 객체는 캡슐화를 위해 생성자가 닫혀있으므로, reconstitute()를 직접 호출해 복원합니다.
    default Manager toDomain(ManagerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Manager.reconstitute(
                entity.getId(),
                entity.getLoginId(),
                entity.getPassword(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRole(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}