package com.kidmily.algoga_server.banner.infrastructure.mapper;

import com.kidmily.algoga_server.banner.domain.model.Banner;
import com.kidmily.algoga_server.banner.infrastructure.persistence.entity.BannerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    BannerEntity toEntity(Banner banner);
    Banner toDomain(BannerEntity entity);
}