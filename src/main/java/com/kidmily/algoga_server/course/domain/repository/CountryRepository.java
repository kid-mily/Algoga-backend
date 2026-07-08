package com.kidmily.algoga_server.course.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.Country;

import java.util.List;
import java.util.Optional;

public interface CountryRepository {
    List<Country> findAllActive();
    Optional<Country> findById(Long id);
    List<Country> findAllByIdIn(List<Long> ids);
}