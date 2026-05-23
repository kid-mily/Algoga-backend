package com.kidmily.algoga_server.country.infrastructure.mapper;

import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.infrastructure.persistence.CountryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {

    public Country toDomain(CountryJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        return Country.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getContinent(),
                jpaEntity.getName(),
                jpaEntity.isActive()
        );
    }
}