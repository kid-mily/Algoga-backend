-- Module 5 presentation data cleanup
-- Purpose:
-- 1) Keep the existing DB, but remove statistics-breaking orphan/duplicate learning records.
-- 2) Normalize signup_path values to the frontend option set.
-- 3) Store reversible backups in permanent backup tables before changing data.
--
-- Run this before/after module5-demo-data.sql if the admin statistics screen shows
-- completion rates over 100%, oversized 기타 inflow, or unnatural orphan learning records.

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 0. Backup rows touched by this cleanup. These tables are intentionally permanent
--    so module5-demo-data-cleanup-rollback.sql can restore them after COMMIT.
--    If the cleanup is run more than once, the first backup is preserved.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m5_cleanup_backup_users_20260725 AS
SELECT *
FROM users
WHERE signup_path IS NULL
   OR TRIM(signup_path) = ''
   OR signup_path NOT IN ('검색 엔진', '소셜 미디어', '지인 추천', '광고', '기타');

CREATE TABLE IF NOT EXISTS m5_cleanup_backup_course_completions_20260725 AS
SELECT cc.*
FROM course_completions cc
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = cc.lecture_id
      AND e.user_id = cc.user_id
)
   OR EXISTS (
    SELECT 1
    FROM course_completions older
    WHERE older.lecture_id = cc.lecture_id
      AND older.user_id = cc.user_id
      AND older.completion_id < cc.completion_id
);

CREATE TABLE IF NOT EXISTS m5_cleanup_backup_learning_progresses_20260725 AS
SELECT lp.*
FROM learning_progresses lp
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = lp.lecture_id
      AND e.user_id = lp.user_id
);

-- -----------------------------------------------------------------------------
-- 1. Normalize signup paths to the exact frontend option labels.
--    NULL/blank is treated as 검색 엔진 instead of 기타 because the current DB has
--    a high-volume legacy user with NULL signup_path, which makes 기타 dominate.
-- -----------------------------------------------------------------------------
UPDATE users
SET signup_path = CASE
    WHEN signup_path IS NULL OR TRIM(signup_path) = '' THEN '검색 엔진'
    WHEN LOWER(REPLACE(TRIM(signup_path), ' ', '')) IN ('friend', 'referral', 'referrercode', '추천인코드', '친구초대', '지인추천') THEN '지인 추천'
    WHEN LOWER(REPLACE(TRIM(signup_path), ' ', '')) IN ('social', 'socialmedia', 'sns', 'instagram', 'youtube', '인스타그램', '유튜브', '소셜미디어') THEN '소셜 미디어'
    WHEN LOWER(REPLACE(TRIM(signup_path), ' ', '')) IN ('search', 'searchengine', 'naver', 'google', 'blog', '네이버검색', '검색엔진', '블로그후기') THEN '검색 엔진'
    WHEN LOWER(REPLACE(TRIM(signup_path), ' ', '')) IN ('ad', 'ads', 'advertisement', 'kakaoad', 'campaign', '광고', '카카오광고') THEN '광고'
    WHEN LOWER(REPLACE(TRIM(signup_path), ' ', '')) IN ('etc', 'other', '기타') THEN '기타'
    ELSE '기타'
END
WHERE signup_path IS NULL
   OR TRIM(signup_path) = ''
   OR signup_path NOT IN ('검색 엔진', '소셜 미디어', '지인 추천', '광고', '기타');

-- -----------------------------------------------------------------------------
-- 2. Remove course completion records that do not have a matching enrollment.
--    This prevents course completion counts from exceeding enrollment counts.
-- -----------------------------------------------------------------------------
DELETE cc
FROM course_completions cc
LEFT JOIN enrollments e
  ON e.lecture_id = cc.lecture_id
 AND e.user_id = cc.user_id
WHERE e.enrollment_id IS NULL;

-- Remove duplicate completion rows per user/course, keeping the oldest row.
DELETE cc
FROM course_completions cc
JOIN course_completions older
  ON older.lecture_id = cc.lecture_id
 AND older.user_id = cc.user_id
 AND older.completion_id < cc.completion_id;

-- -----------------------------------------------------------------------------
-- 3. Remove learning progress records that do not have a matching enrollment.
--    This keeps progress/interest statistics aligned with actual enrollments.
-- -----------------------------------------------------------------------------
DELETE lp
FROM learning_progresses lp
LEFT JOIN enrollments e
  ON e.lecture_id = lp.lecture_id
 AND e.user_id = lp.user_id
WHERE e.enrollment_id IS NULL;

-- -----------------------------------------------------------------------------
-- 4. Validation result set. All counts below should be 0 after cleanup.
-- -----------------------------------------------------------------------------
SELECT 'completion_without_enrollment' AS check_name, COUNT(*) AS issue_count
FROM course_completions cc
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = cc.lecture_id
      AND e.user_id = cc.user_id
)
UNION ALL
SELECT 'duplicate_course_completion', COUNT(*)
FROM (
    SELECT lecture_id, user_id
    FROM course_completions
    GROUP BY lecture_id, user_id
    HAVING COUNT(*) > 1
) duplicated
UNION ALL
SELECT 'learning_progress_without_enrollment', COUNT(*)
FROM learning_progresses lp
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = lp.lecture_id
      AND e.user_id = lp.user_id
)
UNION ALL
SELECT 'invalid_signup_path', COUNT(*)
FROM users
WHERE signup_path IS NULL
   OR TRIM(signup_path) = ''
   OR signup_path NOT IN ('검색 엔진', '소셜 미디어', '지인 추천', '광고', '기타');

-- Course completion rates after cleanup. completion_rate should never exceed 100.
SELECT l.lecture_id,
       l.title,
       COUNT(DISTINCT e.user_id) AS enrolled_users,
       COUNT(DISTINCT cc.user_id) AS completed_users,
       ROUND(COUNT(DISTINCT cc.user_id) / NULLIF(COUNT(DISTINCT e.user_id), 0) * 100, 2) AS completion_rate
FROM lectures l
LEFT JOIN enrollments e ON e.lecture_id = l.lecture_id
LEFT JOIN course_completions cc
  ON cc.lecture_id = l.lecture_id
 AND cc.user_id = e.user_id
WHERE l.status = 'PUBLISHED'
  AND COALESCE(l.is_deleted, false) = false
GROUP BY l.lecture_id, l.title
HAVING completion_rate > 100;

COMMIT;