-- Rollback for Module 5 presentation seed data.
-- Deletes only rows connected to seeded users, technical keys, and generated presentation identifiers.

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS rb_users;
CREATE TEMPORARY TABLE rb_users AS
SELECT user_id
FROM users
WHERE email IN (
    'minji.kang.m5@algoga.local',
    'junho.lee.m5@algoga.local',
    'seoyeon.park.m5@algoga.local',
    'taehyun.kim.m5@algoga.local',
    'yuna.choi.m5@algoga.local',
    'hyunwoo.jung.m5@algoga.local',
    'sumin.han.m5@algoga.local',
    'jaewon.oh.m5@algoga.local',
    'dahye.shin.m5@algoga.local',
    'kihoon.yoon.m5@algoga.local',
    'arin.baek.m5@algoga.local',
    'jisoo.lim.m5@algoga.local'
);

DROP TEMPORARY TABLE IF EXISTS rb_bookings;
CREATE TEMPORARY TABLE rb_bookings AS
SELECT booking_id
FROM bookings
WHERE booking_number LIKE 'M5R202607%';

DROP TEMPORARY TABLE IF EXISTS rb_payments;
CREATE TEMPORARY TABLE rb_payments AS
SELECT payment_id
FROM payments
WHERE idempotency_key LIKE 'M5-%'
   OR user_id IN (SELECT user_id FROM rb_users)
   OR booking_id IN (SELECT booking_id FROM rb_bookings);

DROP TEMPORARY TABLE IF EXISTS rb_qnas;
CREATE TEMPORARY TABLE rb_qnas AS
SELECT qna_id
FROM course_qnas
WHERE user_id IN (SELECT user_id FROM rb_users);

DROP TEMPORARY TABLE IF EXISTS rb_quiz_submissions;
CREATE TEMPORARY TABLE rb_quiz_submissions AS
SELECT submission_id
FROM quiz_submissions
WHERE user_id IN (SELECT user_id FROM rb_users);

DROP TEMPORARY TABLE IF EXISTS rb_coupon_policies;
CREATE TEMPORARY TABLE rb_coupon_policies AS
SELECT DISTINCT coupon_policy_id
FROM user_coupons
WHERE user_id IN (SELECT user_id FROM rb_users)
  AND coupon_policy_id IS NOT NULL;

DELETE FROM refund_requests
WHERE user_id IN (SELECT user_id FROM rb_users)
   OR booking_id IN (SELECT booking_id FROM rb_bookings)
   OR payment_id IN (SELECT payment_id FROM rb_payments);

DELETE FROM payments
WHERE payment_id IN (SELECT payment_id FROM rb_payments);

DELETE FROM bookings
WHERE booking_id IN (SELECT booking_id FROM rb_bookings)
  AND user_id IN (SELECT user_id FROM rb_users);

DELETE FROM course_qna_comments
WHERE qna_id IN (SELECT qna_id FROM rb_qnas)
   OR user_id IN (SELECT user_id FROM rb_users);

DELETE FROM course_qnas
WHERE qna_id IN (SELECT qna_id FROM rb_qnas);

DELETE FROM course_reviews
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM course_rewards
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM mileage_histories
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM user_coupons
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM coupon_policies
WHERE coupon_policy_id IN (SELECT coupon_policy_id FROM rb_coupon_policies)
  AND created_at >= '2026-07-25 00:00:00'
  AND NOT EXISTS (
      SELECT 1
      FROM user_coupons uc
      WHERE uc.coupon_policy_id = coupon_policies.coupon_policy_id
  );

DELETE FROM quiz_submission_answers
WHERE submission_id IN (SELECT submission_id FROM rb_quiz_submissions);

DELETE FROM quiz_submissions
WHERE submission_id IN (SELECT submission_id FROM rb_quiz_submissions);

DELETE FROM course_completions
WHERE user_id IN (SELECT user_id FROM rb_users)
   OR certificate_code LIKE 'ALG-2026-CERT-M5%';

DELETE FROM learning_progresses
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM enrollments
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM diagnosis_results
WHERE user_id IN (SELECT user_id FROM rb_users);

DELETE FROM referral_rewards
WHERE referrer_user_id IN (SELECT user_id FROM rb_users);

DELETE FROM referral_rewards
WHERE referred_user_id IN (SELECT user_id FROM rb_users);

DELETE FROM friend_relations
WHERE requester_id IN (SELECT user_id FROM rb_users);

DELETE FROM friend_relations
WHERE receiver_id IN (SELECT user_id FROM rb_users);

DELETE FROM users
WHERE user_id IN (SELECT user_id FROM rb_users);

DROP TEMPORARY TABLE IF EXISTS rb_quiz_submissions;
DROP TEMPORARY TABLE IF EXISTS rb_coupon_policies;
DROP TEMPORARY TABLE IF EXISTS rb_qnas;
DROP TEMPORARY TABLE IF EXISTS rb_payments;
DROP TEMPORARY TABLE IF EXISTS rb_bookings;
DROP TEMPORARY TABLE IF EXISTS rb_users;

COMMIT;

