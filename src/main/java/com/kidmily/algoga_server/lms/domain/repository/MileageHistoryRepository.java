package com.kidmily.algoga_server.lms.domain.repository;

import com.kidmily.algoga_server.lms.domain.model.MileageHistory;

import java.util.List;
import java.util.Optional;

public interface MileageHistoryRepository {

    MileageHistory save(MileageHistory mileageHistory);

    Optional<MileageHistory> findById(Long mileageHistoryId);

    List<MileageHistory> findByUserId(Long userId);

    List<MileageHistory> findByUserIdAndCourseId(Long userId, Long courseId);
}