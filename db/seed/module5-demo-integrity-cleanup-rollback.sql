-- Rollback for module5-demo-integrity-cleanup.sql
-- Restores rows from permanent backup tables created by the cleanup script.

START TRANSACTION;

INSERT INTO coupon_policies (coupon_policy_id, is_active, coupon_name, lecture_id, created_at, discount_type, discount_value, manager_id, updated_at, valid_days)
SELECT b.coupon_policy_id, b.is_active, b.coupon_name, b.lecture_id, b.created_at, b.discount_type, b.discount_value, b.manager_id, b.updated_at, b.valid_days
FROM m5_integrity_backup_coupon_policies_20260725 b
LEFT JOIN coupon_policies cp ON cp.coupon_policy_id = b.coupon_policy_id
WHERE cp.coupon_policy_id IS NULL;

INSERT INTO bookings (booking_id, accommodation_id, balance_price, booking_number, check_in_date, check_out_date, created_at, deposit_price, flight_info, installment_allowed, nights, passenger_info, return_flight_info, status, total_price, updated_at, user_id, package_id)
SELECT b.booking_id, b.accommodation_id, b.balance_price, b.booking_number, b.check_in_date, b.check_out_date, b.created_at, b.deposit_price, b.flight_info, b.installment_allowed, b.nights, b.passenger_info, b.return_flight_info, b.status, b.total_price, b.updated_at, b.user_id, b.package_id
FROM m5_integrity_backup_bookings_20260725 b
LEFT JOIN bookings cur ON cur.booking_id = b.booking_id
WHERE cur.booking_id IS NULL;

INSERT INTO enrollments (enrollment_id, access_expires_at, completed_at, lecture_id, enrolled_at, status, user_id)
SELECT b.enrollment_id, b.access_expires_at, b.completed_at, b.lecture_id, b.enrolled_at, b.status, b.user_id
FROM m5_integrity_backup_enrollments_20260725 b
LEFT JOIN enrollments cur ON cur.enrollment_id = b.enrollment_id
WHERE cur.enrollment_id IS NULL;

INSERT INTO payments (payment_id, amount, booking_id, course_id, created_at, idempotency_key, payment_method, payment_type, portone_payment_id, status, used_coupon_id, used_mileage, user_id, user_name)
SELECT b.payment_id, b.amount, b.booking_id, b.course_id, b.created_at, b.idempotency_key, b.payment_method, b.payment_type, b.portone_payment_id, b.status, b.used_coupon_id, b.used_mileage, b.user_id, b.user_name
FROM m5_integrity_backup_payments_20260725 b
LEFT JOIN payments cur ON cur.payment_id = b.payment_id
WHERE cur.payment_id IS NULL;

INSERT INTO refund_requests (refund_request_id, amount, booking_id, created_at, payment_id, reason, reject_reason, status, updated_at, user_id, user_name)
SELECT b.refund_request_id, b.amount, b.booking_id, b.created_at, b.payment_id, b.reason, b.reject_reason, b.status, b.updated_at, b.user_id, b.user_name
FROM m5_integrity_backup_refund_requests_20260725 b
LEFT JOIN refund_requests cur ON cur.refund_request_id = b.refund_request_id
WHERE cur.refund_request_id IS NULL;

INSERT INTO payment_attempts (id, accommodation_id, created_at, user_id)
SELECT b.id, b.accommodation_id, b.created_at, b.user_id
FROM m5_integrity_backup_payment_attempts_20260725 b
LEFT JOIN payment_attempts cur ON cur.id = b.id
WHERE cur.id IS NULL;

INSERT INTO learning_progresses (progress_id, chapter_id, is_completed, lecture_id, created_at, progress_rate, updated_at, user_id, watched_seconds)
SELECT b.progress_id, b.chapter_id, b.is_completed, b.lecture_id, b.created_at, b.progress_rate, b.updated_at, b.user_id, b.watched_seconds
FROM m5_integrity_backup_learning_progresses_20260725 b
LEFT JOIN learning_progresses cur ON cur.progress_id = b.progress_id
WHERE cur.progress_id IS NULL;

INSERT INTO course_completions (completion_id, certificate_code, completed_at, lecture_id, user_id)
SELECT b.completion_id, b.certificate_code, b.completed_at, b.lecture_id, b.user_id
FROM m5_integrity_backup_course_completions_20260725 b
LEFT JOIN course_completions cur ON cur.completion_id = b.completion_id
WHERE cur.completion_id IS NULL;

INSERT INTO quiz_submissions (submission_id, correct_count, lecture_id, score, submitted_at, total_count, user_id)
SELECT b.submission_id, b.correct_count, b.lecture_id, b.score, b.submitted_at, b.total_count, b.user_id
FROM m5_integrity_backup_quiz_submissions_20260725 b
LEFT JOIN quiz_submissions cur ON cur.submission_id = b.submission_id
WHERE cur.submission_id IS NULL;

INSERT INTO quiz_submission_answers (answer_id, is_correct, correct_option, explanation, option1, option2, option3, option4, question, quiz_id, selected_option, submission_id)
SELECT b.answer_id, b.is_correct, b.correct_option, b.explanation, b.option1, b.option2, b.option3, b.option4, b.question, b.quiz_id, b.selected_option, b.submission_id
FROM m5_integrity_backup_quiz_submission_answers_20260725 b
LEFT JOIN quiz_submission_answers cur ON cur.answer_id = b.answer_id
WHERE cur.answer_id IS NULL;

INSERT INTO mileage_histories (mileage_history_id, amount, course_id, created_at, expired_at, manager_id, reason, type, user_id)
SELECT b.mileage_history_id, b.amount, b.course_id, b.created_at, b.expired_at, b.manager_id, b.reason, b.type, b.user_id
FROM m5_integrity_backup_mileage_histories_20260725 b
LEFT JOIN mileage_histories cur ON cur.mileage_history_id = b.mileage_history_id
WHERE cur.mileage_history_id IS NULL;

INSERT INTO user_coupons (user_coupon_id, coupon_name, coupon_policy_id, lecture_id, discount_type, discount_value, expired_at, issued_at, status, used_at, user_id)
SELECT b.user_coupon_id, b.coupon_name, b.coupon_policy_id, b.lecture_id, b.discount_type, b.discount_value, b.expired_at, b.issued_at, b.status, b.used_at, b.user_id
FROM m5_integrity_backup_user_coupons_20260725 b
LEFT JOIN user_coupons cur ON cur.user_coupon_id = b.user_coupon_id
WHERE cur.user_coupon_id IS NULL;

INSERT INTO course_rewards (reward_id, lecture_id, issued_coupon_count, mileage_history_id, rewarded_at, user_id)
SELECT b.reward_id, b.lecture_id, b.issued_coupon_count, b.mileage_history_id, b.rewarded_at, b.user_id
FROM m5_integrity_backup_course_rewards_20260725 b
LEFT JOIN course_rewards cur ON cur.reward_id = b.reward_id
WHERE cur.reward_id IS NULL;

COMMIT;