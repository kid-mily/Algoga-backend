package com.kidmily.algoga_server.user.infrastructure.mapper;

import com.kidmily.algoga_server.user.domain.model.User;
import com.kidmily.algoga_server.user.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    // 도메인 객체를 DB 엔티티로 변환 (DB가 자동 생성하는 값들은 매핑 무시)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserJpaEntity toJpaEntity(User user);

    // DB 엔티티를 도메인 객체로 변환 (User.reconstitute 호출)
    default User toDomain(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return User.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getName(),
                jpaEntity.getPhone(),
                jpaEntity.getBirthDate(),
                jpaEntity.getGender(),
                jpaEntity.getNickname(),
                jpaEntity.getSocialType(),
                jpaEntity.getPersonalCode(),
                jpaEntity.getLoginFailCount(),
                jpaEntity.getLockedUntil(),
                jpaEntity.isDeleted()
        );
    }
}