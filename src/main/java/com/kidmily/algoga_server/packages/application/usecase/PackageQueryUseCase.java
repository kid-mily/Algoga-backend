package com.kidmily.algoga_server.packages.application.usecase;

import com.kidmily.algoga_server.packages.presentation.api.response.PackageDetailResponse;
import com.kidmily.algoga_server.packages.presentation.api.response.PackageListResponse;

import java.util.List;

public interface PackageQueryUseCase {

    List<PackageListResponse> getPackagesByCountry(Long countryId);

    PackageDetailResponse getPackageDetail(Long packageId);
}