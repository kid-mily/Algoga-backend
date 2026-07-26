package com.kidmily.algoga_server.benefit.domain.repository;

import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MileageHistoryRepository {

    MileageHistory save(MileageHistory mileageHistory);

    List<MileageHistory> findByUserId(Long userId);

    Page<MileageHistory> findByUserId(Long userId, Pageable pageable);

    List<MileageHistory> findByUserIdIn(List<Long> userIds);

    /**
     * 마일리지 내역이 있는 사용자 ID를 페이지 단위로 조회한다.
     * (기존엔 findAll()로 마일리지 내역 전체를 로드해서 메모리에서 distinct userId를 뽑았다)
     */
    Page<Long> findDistinctUserIds(Pageable pageable);

    /**
     * 페이지와 무관한 전체 합계(사용자 수/총 적립/총 사용)를 DB에서 직접 집계한다.
     * 사용자별 목록은 페이징되지만 요약 수치는 항상 전체 기준이어야 하기 때문이다.
     */
    GlobalMileageTotals findGlobalTotals(LocalDateTime now);

    void deleteAllByUserId(Long userId);

    record GlobalMileageTotals(long userCount, int totalEarnedMileage, int totalUsedMileage) {
    }
}
