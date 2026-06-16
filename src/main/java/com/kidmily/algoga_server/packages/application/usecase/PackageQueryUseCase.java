package com.kidmily.algoga_server.packages.application.usecase;

import com.kidmily.algoga_server.packages.presentation.api.response.PackageResponse;

import java.util.List;

public interface PackageQueryUseCase {
    List<PackageResponse> getAll();
    List<PackageResponse> getByCountry(Long countryId);
    PackageResponse getById(Long packageId);
}
