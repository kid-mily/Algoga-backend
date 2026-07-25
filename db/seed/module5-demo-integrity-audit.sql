-- Module 5 presentation DB integrity audit
-- Read-only checks for statistics-breaking data.
-- Run after cleanup SQL files to verify the DB is safe for manager presentation screens.

-- 1. Learning/completion consistency
SELECT 'course_completions_without_enrollment' AS check_name, COUNT(*) AS issue_count
FROM course_completions cc
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = cc.lecture_id
      AND e.user_id = cc.user_id
)
UNION ALL
SELECT 'duplicate_course_completion_pairs', COUNT(*)
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
SELECT 'quiz_submission_without_enrollment', COUNT(*)
FROM quiz_submissions qs
WHERE NOT EXISTS (
    SELECT 1
    FROM enrollments e
    WHERE e.lecture_id = qs.lecture_id
      AND e.user_id = qs.user_id
);

-- 2. Completion rate must not exceed 100%.
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

-- 3. Statistics source rows that reference users missing from users table.
SELECT 'bookings_missing_user' AS check_name, COUNT(*) AS issue_count
FROM bookings b
WHERE b.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = b.user_id)
UNION ALL
SELECT 'payments_missing_user', COUNT(*)
FROM payments p
WHERE p.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = p.user_id)
UNION ALL
SELECT 'refund_requests_missing_user', COUNT(*)
FROM refund_requests r
WHERE r.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = r.user_id)
UNION ALL
SELECT 'enrollments_missing_user', COUNT(*)
FROM enrollments e
WHERE e.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = e.user_id)
UNION ALL
SELECT 'course_completions_missing_user', COUNT(*)
FROM course_completions cc
WHERE cc.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = cc.user_id)
UNION ALL
SELECT 'learning_progresses_missing_user', COUNT(*)
FROM learning_progresses lp
WHERE lp.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = lp.user_id);

-- 4. Booking rows with missing accommodation.
SELECT b.booking_id,
       b.booking_number,
       b.accommodation_id,
       b.user_id,
       b.created_at,
       b.status
FROM bookings b
LEFT JOIN accommodations a ON a.accommodation_id = b.accommodation_id
WHERE b.accommodation_id IS NOT NULL
  AND a.accommodation_id IS NULL
ORDER BY b.booking_id;

-- 5. Booking/profit countries that do not have active published lectures.
-- This should return no rows after module5-demo-country-cleanup.sql.
SELECT c.country_id,
       c.name,
       COUNT(DISTINCT b.booking_id) AS booking_count
FROM countries c
JOIN accommodations a ON a.country_id = c.country_id
JOIN bookings b ON b.accommodation_id = a.accommodation_id
WHERE NOT EXISTS (
    SELECT 1
    FROM lectures l
    WHERE l.country_id = c.country_id
      AND l.status = 'PUBLISHED'
      AND COALESCE(l.is_deleted, false) = false
)
GROUP BY c.country_id, c.name
ORDER BY booking_count DESC;

-- 6. LMS rows referencing non-active lectures.
SELECT 'enrollments_non_active_lecture' AS check_name, COUNT(*) AS issue_count
FROM enrollments e
JOIN lectures l ON l.lecture_id = e.lecture_id
WHERE l.status <> 'PUBLISHED'
   OR COALESCE(l.is_deleted, false) = true
UNION ALL
SELECT 'learning_progresses_non_active_lecture', COUNT(*)
FROM learning_progresses lp
JOIN lectures l ON l.lecture_id = lp.lecture_id
WHERE l.status <> 'PUBLISHED'
   OR COALESCE(l.is_deleted, false) = true
UNION ALL
SELECT 'course_completions_non_active_lecture', COUNT(*)
FROM course_completions cc
JOIN lectures l ON l.lecture_id = cc.lecture_id
WHERE l.status <> 'PUBLISHED'
   OR COALESCE(l.is_deleted, false) = true
UNION ALL
SELECT 'quiz_submissions_non_active_lecture', COUNT(*)
FROM quiz_submissions qs
JOIN lectures l ON l.lecture_id = qs.lecture_id
WHERE l.status <> 'PUBLISHED'
   OR COALESCE(l.is_deleted, false) = true
UNION ALL
SELECT 'payments_non_active_course', COUNT(*)
FROM payments p
JOIN lectures l ON l.lecture_id = p.course_id
WHERE p.course_id IS NOT NULL
  AND (l.status <> 'PUBLISHED' OR COALESCE(l.is_deleted, false) = true);

-- 7. Signup path must match frontend options.
SELECT signup_path, COUNT(*) AS user_count
FROM users
GROUP BY signup_path
ORDER BY user_count DESC;

SELECT 'invalid_signup_path' AS check_name, COUNT(*) AS issue_count
FROM users
WHERE signup_path IS NULL
   OR TRIM(signup_path) = ''
   OR signup_path NOT IN ('검색 엔진', '소셜 미디어', '지인 추천', '광고', '기타');