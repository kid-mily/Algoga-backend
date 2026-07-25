-- Module 5 presentation integrity cleanup
-- Removes statistics-breaking rows that reference missing users, missing accommodations,
-- or non-active/deleted lectures. Every touched table is backed up first.

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 0. Identify bad source ids.
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS m5_bad_users;
CREATE TEMPORARY TABLE m5_bad_users (user_id BIGINT PRIMARY KEY);
INSERT IGNORE INTO m5_bad_users (user_id)
SELECT DISTINCT b.user_id
FROM bookings b
LEFT JOIN users u ON u.user_id = b.user_id
WHERE b.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT p.user_id
FROM payments p
LEFT JOIN users u ON u.user_id = p.user_id
WHERE p.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT r.user_id
FROM refund_requests r
LEFT JOIN users u ON u.user_id = r.user_id
WHERE r.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT e.user_id
FROM enrollments e
LEFT JOIN users u ON u.user_id = e.user_id
WHERE e.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT cc.user_id
FROM course_completions cc
LEFT JOIN users u ON u.user_id = cc.user_id
WHERE cc.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT lp.user_id
FROM learning_progresses lp
LEFT JOIN users u ON u.user_id = lp.user_id
WHERE lp.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT pa.user_id
FROM payment_attempts pa
LEFT JOIN users u ON u.user_id = pa.user_id
WHERE pa.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT uc.user_id
FROM user_coupons uc
LEFT JOIN users u ON u.user_id = uc.user_id
WHERE uc.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT mh.user_id
FROM mileage_histories mh
LEFT JOIN users u ON u.user_id = mh.user_id
WHERE mh.user_id IS NOT NULL AND u.user_id IS NULL
UNION
SELECT DISTINCT cr.user_id
FROM course_rewards cr
LEFT JOIN users u ON u.user_id = cr.user_id
WHERE cr.user_id IS NOT NULL AND u.user_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS m5_bad_bookings;
CREATE TEMPORARY TABLE m5_bad_bookings (booking_id BIGINT PRIMARY KEY);
INSERT IGNORE INTO m5_bad_bookings (booking_id)
SELECT DISTINCT b.booking_id
FROM bookings b
LEFT JOIN users u ON u.user_id = b.user_id
LEFT JOIN accommodations a ON a.accommodation_id = b.accommodation_id
WHERE u.user_id IS NULL
   OR a.accommodation_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS m5_bad_lectures;
CREATE TEMPORARY TABLE m5_bad_lectures (lecture_id BIGINT PRIMARY KEY);
INSERT IGNORE INTO m5_bad_lectures (lecture_id)
SELECT lecture_id
FROM lectures
WHERE status <> 'PUBLISHED'
   OR COALESCE(is_deleted, false) = true;

DROP TEMPORARY TABLE IF EXISTS m5_bad_quiz_submissions;
CREATE TEMPORARY TABLE m5_bad_quiz_submissions (submission_id BIGINT PRIMARY KEY);
INSERT IGNORE INTO m5_bad_quiz_submissions (submission_id)
SELECT DISTINCT qs.submission_id
FROM quiz_submissions qs
LEFT JOIN users u ON u.user_id = qs.user_id
LEFT JOIN enrollments e ON e.lecture_id = qs.lecture_id AND e.user_id = qs.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = qs.lecture_id
WHERE u.user_id IS NULL
   OR e.enrollment_id IS NULL
   OR bl.lecture_id IS NOT NULL;

-- -----------------------------------------------------------------------------
-- 1. Permanent backups for rollback.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m5_integrity_backup_bookings_20260725 AS
SELECT b.*
FROM bookings b
JOIN m5_bad_bookings bad ON bad.booking_id = b.booking_id;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_payments_20260725 AS
SELECT p.*
FROM payments p
LEFT JOIN m5_bad_bookings bad_booking ON bad_booking.booking_id = p.booking_id
LEFT JOIN m5_bad_users bad_user ON bad_user.user_id = p.user_id
LEFT JOIN m5_bad_lectures bad_lecture ON bad_lecture.lecture_id = p.course_id
WHERE bad_booking.booking_id IS NOT NULL
   OR bad_user.user_id IS NOT NULL
   OR bad_lecture.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_refund_requests_20260725 AS
SELECT r.*
FROM refund_requests r
LEFT JOIN m5_bad_bookings bad_booking ON bad_booking.booking_id = r.booking_id
LEFT JOIN m5_bad_users bad_user ON bad_user.user_id = r.user_id
WHERE bad_booking.booking_id IS NOT NULL
   OR bad_user.user_id IS NOT NULL
   OR NOT EXISTS (SELECT 1 FROM payments p WHERE p.payment_id = r.payment_id);

CREATE TABLE IF NOT EXISTS m5_integrity_backup_payment_attempts_20260725 AS
SELECT pa.*
FROM payment_attempts pa
LEFT JOIN users u ON u.user_id = pa.user_id
LEFT JOIN accommodations a ON a.accommodation_id = pa.accommodation_id
WHERE u.user_id IS NULL
   OR a.accommodation_id IS NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_enrollments_20260725 AS
SELECT e.*
FROM enrollments e
LEFT JOIN users u ON u.user_id = e.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = e.lecture_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_course_completions_20260725 AS
SELECT cc.*
FROM course_completions cc
LEFT JOIN users u ON u.user_id = cc.user_id
LEFT JOIN enrollments e ON e.lecture_id = cc.lecture_id AND e.user_id = cc.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = cc.lecture_id
WHERE u.user_id IS NULL
   OR e.enrollment_id IS NULL
   OR bl.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_learning_progresses_20260725 AS
SELECT lp.*
FROM learning_progresses lp
LEFT JOIN users u ON u.user_id = lp.user_id
LEFT JOIN enrollments e ON e.lecture_id = lp.lecture_id AND e.user_id = lp.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = lp.lecture_id
WHERE u.user_id IS NULL
   OR e.enrollment_id IS NULL
   OR bl.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_quiz_submissions_20260725 AS
SELECT qs.*
FROM quiz_submissions qs
JOIN m5_bad_quiz_submissions bad ON bad.submission_id = qs.submission_id;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_quiz_submission_answers_20260725 AS
SELECT qsa.*
FROM quiz_submission_answers qsa
JOIN m5_bad_quiz_submissions bad ON bad.submission_id = qsa.submission_id;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_course_rewards_20260725 AS
SELECT cr.*
FROM course_rewards cr
LEFT JOIN users u ON u.user_id = cr.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = cr.lecture_id
LEFT JOIN mileage_histories mh ON mh.mileage_history_id = cr.mileage_history_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL
   OR mh.mileage_history_id IS NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_mileage_histories_20260725 AS
SELECT mh.*
FROM mileage_histories mh
LEFT JOIN users u ON u.user_id = mh.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = mh.course_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_user_coupons_20260725 AS
SELECT uc.*
FROM user_coupons uc
LEFT JOIN users u ON u.user_id = uc.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = uc.lecture_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS m5_integrity_backup_coupon_policies_20260725 AS
SELECT cp.*
FROM coupon_policies cp
JOIN m5_bad_lectures bl ON bl.lecture_id = cp.lecture_id;

-- -----------------------------------------------------------------------------
-- 2. Delete bad rows, child-ish tables first.
-- -----------------------------------------------------------------------------
DELETE qsa
FROM quiz_submission_answers qsa
JOIN m5_bad_quiz_submissions bad ON bad.submission_id = qsa.submission_id;

DELETE qs
FROM quiz_submissions qs
JOIN m5_bad_quiz_submissions bad ON bad.submission_id = qs.submission_id;

DELETE cr
FROM course_rewards cr
LEFT JOIN users u ON u.user_id = cr.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = cr.lecture_id
LEFT JOIN mileage_histories mh ON mh.mileage_history_id = cr.mileage_history_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL
   OR mh.mileage_history_id IS NULL;

DELETE uc
FROM user_coupons uc
LEFT JOIN users u ON u.user_id = uc.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = uc.lecture_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

DELETE mh
FROM mileage_histories mh
LEFT JOIN users u ON u.user_id = mh.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = mh.course_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

DELETE rr
FROM refund_requests rr
LEFT JOIN m5_bad_bookings bad_booking ON bad_booking.booking_id = rr.booking_id
LEFT JOIN m5_bad_users bad_user ON bad_user.user_id = rr.user_id
LEFT JOIN payments p ON p.payment_id = rr.payment_id
WHERE bad_booking.booking_id IS NOT NULL
   OR bad_user.user_id IS NOT NULL
   OR p.payment_id IS NULL;

DELETE p
FROM payments p
LEFT JOIN m5_bad_bookings bad_booking ON bad_booking.booking_id = p.booking_id
LEFT JOIN m5_bad_users bad_user ON bad_user.user_id = p.user_id
LEFT JOIN m5_bad_lectures bad_lecture ON bad_lecture.lecture_id = p.course_id
WHERE bad_booking.booking_id IS NOT NULL
   OR bad_user.user_id IS NOT NULL
   OR bad_lecture.lecture_id IS NOT NULL;

DELETE pa
FROM payment_attempts pa
LEFT JOIN users u ON u.user_id = pa.user_id
LEFT JOIN accommodations a ON a.accommodation_id = pa.accommodation_id
WHERE u.user_id IS NULL
   OR a.accommodation_id IS NULL;

DELETE lp
FROM learning_progresses lp
LEFT JOIN users u ON u.user_id = lp.user_id
LEFT JOIN enrollments e ON e.lecture_id = lp.lecture_id AND e.user_id = lp.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = lp.lecture_id
WHERE u.user_id IS NULL
   OR e.enrollment_id IS NULL
   OR bl.lecture_id IS NOT NULL;

DELETE cc
FROM course_completions cc
LEFT JOIN users u ON u.user_id = cc.user_id
LEFT JOIN enrollments e ON e.lecture_id = cc.lecture_id AND e.user_id = cc.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = cc.lecture_id
WHERE u.user_id IS NULL
   OR e.enrollment_id IS NULL
   OR bl.lecture_id IS NOT NULL;

DELETE e
FROM enrollments e
LEFT JOIN users u ON u.user_id = e.user_id
LEFT JOIN m5_bad_lectures bl ON bl.lecture_id = e.lecture_id
WHERE u.user_id IS NULL
   OR bl.lecture_id IS NOT NULL;

DELETE b
FROM bookings b
JOIN m5_bad_bookings bad ON bad.booking_id = b.booking_id;

DELETE cp
FROM coupon_policies cp
JOIN m5_bad_lectures bl ON bl.lecture_id = cp.lecture_id
WHERE NOT EXISTS (
    SELECT 1
    FROM user_coupons uc
    WHERE uc.coupon_policy_id = cp.coupon_policy_id
);

-- -----------------------------------------------------------------------------
-- 3. Validation. All issue_count values should be 0, and row result sets should be empty.
-- -----------------------------------------------------------------------------
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
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = lp.user_id)
UNION ALL
SELECT 'payment_attempts_missing_user', COUNT(*)
FROM payment_attempts pa
WHERE pa.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = pa.user_id)
UNION ALL
SELECT 'user_coupons_missing_user', COUNT(*)
FROM user_coupons uc
WHERE uc.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = uc.user_id);

SELECT b.booking_id, b.booking_number, b.accommodation_id, b.user_id, b.created_at, b.status
FROM bookings b
LEFT JOIN accommodations a ON a.accommodation_id = b.accommodation_id
WHERE b.accommodation_id IS NOT NULL
  AND a.accommodation_id IS NULL
ORDER BY b.booking_id;

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

COMMIT;