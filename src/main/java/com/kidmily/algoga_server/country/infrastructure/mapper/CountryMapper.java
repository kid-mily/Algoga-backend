package com.kidmily.algoga_server.country.infrastructure.mapper;

import com.kidmily.algoga_server.country.domain.model.Country;
import com.kidmily.algoga_server.country.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {

    public Country toDomain(CountryJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        return Country.withId(
                jpaEntity.getId(),
                jpaEntity.getCountryCode(),
                jpaEntity.getContinentCode(),
                jpaEntity.getContinentName(),
                jpaEntity.getName(),
                jpaEntity.getActive() != null && jpaEntity.getActive()
        );
    }
}