package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;

import java.util.List;

public interface MileageHistoryRepository {

    MileageHistory save(MileageHistory mileageHistory);

    List<MileageHistory> findAll();

    List<MileageHistory> findByUserId(Long userId);
}