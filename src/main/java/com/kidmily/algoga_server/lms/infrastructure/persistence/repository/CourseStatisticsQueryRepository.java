package com.kidmily.algoga_server.lms.infrastructure.persistence.repository;

import com.kidmily.algoga_server.lms.application.port.CourseStatisticsQueryPort;
import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsPageResult;
import com.kidmily.algoga_server.lms.application.result.CourseEnrollmentStatisticsResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseStatisticsQueryRepository implements CourseStatisticsQueryPort {

    private final EntityManager entityManager;

    @Override
    public CourseEnrollmentStatisticsPageResult findCourseEnrollmentStatistics(
            String keyword,
            int page,
            int size
    ) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String keywordLike = hasKeyword ? "%" + keyword.trim() + "%" : null;
        int offset = page * size;

        String whereSql = hasKeyword
                ? " WHERE l.is_deleted = 0 AND l.title LIKE :keyword "
                : " WHERE l.is_deleted = 0 ";

        String contentSql = """
                SELECT
                    l.lecture_id,
                    l.title,
                    COUNT(DISTINCT e.user_id),
                    COALESCE(ROUND(AVG(up.user_progress_rate)), 0),
                    CASE
                        WHEN COUNT(DISTINCT e.user_id) = 0 THEN 0
                        ELSE ROUND(COUNT(DISTINCT cc.user_id) * 100.0 / COUNT(DISTINCT e.user_id))
                    END,
                    COALESCE(ROUND(SUM(COALESCE(up.user_watched_seconds, 0)) / 3600.0 / NULLIF(COUNT(DISTINCT e.user_id), 0), 1), 0)
                FROM lectures l
                LEFT JOIN enrollments e
                    ON e.lecture_id = l.lecture_id
                LEFT JOIN (
                    SELECT
                        lecture_id,
                        user_id,
                        AVG(progress_rate) AS user_progress_rate,
                        SUM(watched_seconds) AS user_watched_seconds
                    FROM learning_progresses
                    GROUP BY lecture_id, user_id
                ) up
                    ON up.lecture_id = l.lecture_id
                   AND up.user_id = e.user_id
                LEFT JOIN course_completions cc
                    ON cc.lecture_id = l.lecture_id
                   AND cc.user_id = e.user_id
                """ + whereSql + """
                GROUP BY l.lecture_id, l.title
                ORDER BY COUNT(DISTINCT e.user_id) DESC, l.lecture_id DESC
                LIMIT :limit OFFSET :offset
                """;

        Query contentQuery = entityManager.createNativeQuery(contentSql)
                .setParameter("limit", size)
                .setParameter("offset", offset);

        if (hasKeyword) {
            contentQuery.setParameter("keyword", keywordLike);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = contentQuery.getResultList();

        List<CourseEnrollmentStatisticsResult> content = rows.stream()
                .map(row -> new CourseEnrollmentStatisticsResult(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).intValue(),
                        ((Number) row[4]).intValue(),
                        ((Number) row[5]).doubleValue()
                ))
                .toList();

        String countSql = """
                SELECT COUNT(*)
                FROM lectures l
                """ + whereSql;

        Query countQuery = entityManager.createNativeQuery(countSql);

        if (hasKeyword) {
            countQuery.setParameter("keyword", keywordLike);
        }

        long totalElements = ((Number) countQuery.getSingleResult()).longValue();

        return new CourseEnrollmentStatisticsPageResult(
                totalElements,
                page,
                size,
                content
        );
    }
}