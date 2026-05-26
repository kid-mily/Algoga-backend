package com.kidmily.algoga_server.calendar.infrastructure.adapter;

import com.kidmily.algoga_server.calendar.application.port.PackagePort;
import com.kidmily.algoga_server.packages.domain.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PackagePortAdapter implements PackagePort {

    private final PackageRepository packageRepository;

    @Override
    public String getPackageName(Long packageId) {
        return packageRepository.findById(packageId)
                .map(pkg -> pkg.getName())
                .orElse("삭제된 패키지");
    }
}