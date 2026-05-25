package com.kidmily.algoga_server.country.domain.repository;
import com.kidmily.algoga_server.country.domain.model.Country;
import java.util.List;

public interface CountryRepository {
    List<Country> findAllActive();

}
