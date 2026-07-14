package com.kidmily.algoga_server.stats.domain.model;

/**
 * 돈 요약 추이 그래프의 집계 단위.
 * FE가 기간 프리셋에 맞춰 선택한다: 오늘→HOUR / 이번주·이번달→DAY / 올해→MONTH.
 */
public enum TrendUnit {
    HOUR,
    DAY,
    MONTH
}
