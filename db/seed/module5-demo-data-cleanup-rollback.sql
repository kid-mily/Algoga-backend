-- Rollback for module5-demo-data-cleanup.sql
-- Restores rows from permanent backup tables created by the cleanup script.

START TRANSACTION;

-- Restore original signup_path values for users touched by cleanup.
UPDATE users u
JOIN m5_cleanup_backup_users_20260725 b ON b.user_id = u.user_id
SET u.signup_path = b.signup_path;

-- Restore course completion rows deleted by cleanup.
INSERT INTO course_completions (completion_id, certificate_code, completed_at, lecture_id, user_id)
SELECT b.completion_id, b.certificate_code, b.completed_at, b.lecture_id, b.user_id
FROM m5_cleanup_backup_course_completions_20260725 b
LEFT JOIN course_completions cc ON cc.completion_id = b.completion_id
WHERE cc.completion_id IS NULL;

-- Restore learning progress rows deleted by cleanup.
INSERT INTO learning_progresses (
    progress_id, chapter_id, is_completed, lecture_id, created_at,
    progress_rate, updated_at, user_id, watched_seconds
)
SELECT b.progress_id, b.chapter_id, b.is_completed, b.lecture_id, b.created_at,
       b.progress_rate, b.updated_at, b.user_id, b.watched_seconds
FROM m5_cleanup_backup_learning_progresses_20260725 b
LEFT JOIN learning_progresses lp ON lp.progress_id = b.progress_id
WHERE lp.progress_id IS NULL;

-- Keep backup tables by default. Drop them manually only after verifying rollback is unnecessary.
-- DROP TABLE IF EXISTS m5_cleanup_backup_users_20260725;
-- DROP TABLE IF EXISTS m5_cleanup_backup_course_completions_20260725;
-- DROP TABLE IF EXISTS m5_cleanup_backup_learning_progresses_20260725;

COMMIT;