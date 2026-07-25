-- Module 5 presentation seed data
-- Password for all seeded users: password123
-- Visible Korean contents are written as realistic service data. Technical keys use M5 only for rollback.

START TRANSACTION;

SET @seed_tag := 'M5';
SET @now := NOW();
SET @user_password := '$2a$10$doePg0geQkvqcqPB71CCIO1subLw5o6etcVjhApajVjMB3a5TjHWO';

SET @content_manager_id := (
    SELECT manager_id
    FROM managers
    WHERE is_deleted = false
      AND role IN ('CONTENT_MANAGER', 'SUPER_ADMIN')
    ORDER BY CASE role WHEN 'CONTENT_MANAGER' THEN 0 ELSE 1 END, manager_id
    LIMIT 1
);
SET @cs_manager_id := (
    SELECT manager_id
    FROM managers
    WHERE is_deleted = false
      AND role IN ('CS_MANAGER', 'SUPER_ADMIN')
    ORDER BY CASE role WHEN 'CS_MANAGER' THEN 0 ELSE 1 END, manager_id
    LIMIT 1
);
SET @settlement_manager_id := (
    SELECT manager_id
    FROM managers
    WHERE is_deleted = false
      AND role IN ('SETTLEMENT_MANAGER', 'SUPER_ADMIN')
    ORDER BY CASE role WHEN 'SETTLEMENT_MANAGER' THEN 0 ELSE 1 END, manager_id
    LIMIT 1
);
SET @manager_id := COALESCE(@content_manager_id, @cs_manager_id, @settlement_manager_id, 1);

DROP TEMPORARY TABLE IF EXISTS seed_courses;
CREATE TEMPORARY TABLE seed_courses (
    seq INT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    reward_mileage INT NOT NULL,
    level VARCHAR(30) NOT NULL
);

INSERT INTO seed_courses (seq, course_id, country_id, title, price, reward_mileage, level)
SELECT ROW_NUMBER() OVER (ORDER BY l.lecture_id) AS seq,
       l.lecture_id,
       l.country_id,
       l.title,
       l.price,
       COALESCE(l.max_reward_mileage, 0),
       l.level
FROM lectures l
JOIN countries country ON country.country_id = l.country_id AND country.is_active = true
WHERE l.status = 'PUBLISHED'
  AND COALESCE(l.is_deleted, false) = false
  AND country.name NOT IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
  AND EXISTS (
      SELECT 1
      FROM accommodations a
      WHERE a.country_id = l.country_id
        AND a.price_per_night IS NOT NULL
        AND a.nights IS NOT NULL
  )
  AND EXISTS (
      SELECT 1
      FROM chapters c
      WHERE c.lecture_id = l.lecture_id
        AND COALESCE(c.is_deleted, false) = false
  )
  AND EXISTS (
      SELECT 1
      FROM quizzes q
      WHERE q.lecture_id = l.lecture_id
        AND COALESCE(q.is_deleted, false) = false
  )
ORDER BY l.lecture_id
LIMIT 8;

SET @course_count := (SELECT COUNT(*) FROM seed_courses);

DROP TEMPORARY TABLE IF EXISTS seed_assert_courses;
CREATE TEMPORARY TABLE seed_assert_courses (id INT NOT NULL);
INSERT INTO seed_assert_courses (id)
SELECT NULL
WHERE @course_count = 0;
DROP TEMPORARY TABLE IF EXISTS seed_assert_courses;

DROP TEMPORARY TABLE IF EXISTS seed_users;
CREATE TEMPORARY TABLE seed_users (
    seq INT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    personal_code VARCHAR(20) NOT NULL,
    referral_code VARCHAR(20),
    signup_path VARCHAR(50) NOT NULL,
    marketing_agreed BOOLEAN NOT NULL
);

INSERT INTO seed_users VALUES
(1, 'minji.kang.m5', 'minji.kang.m5@algoga.local', '강민지', '010-7241-1830', '1994-03-12', 'FEMALE', '민지', 'M5A2B3', NULL, '검색 엔진', true),
(2, 'junho.lee.m5', 'junho.lee.m5@algoga.local', '이준호', '010-6382-5194', '1991-08-24', 'MALE', '준호', 'M5C4D5', 'M5A2B3', '지인 추천', true),
(3, 'seoyeon.park.m5', 'seoyeon.park.m5@algoga.local', '박서연', '010-4829-3716', '1997-11-05', 'FEMALE', '서연', 'M5E6F7', 'M5A2B3', '소셜 미디어', false),
(4, 'taehyun.kim.m5', 'taehyun.kim.m5@algoga.local', '김태현', '010-9153-2048', '1989-06-18', 'MALE', '태현', 'M5G8H9', 'M5C4D5', '검색 엔진', true),
(5, 'yuna.choi.m5', 'yuna.choi.m5@algoga.local', '최유나', '010-3571-8402', '1996-01-29', 'FEMALE', '유나', 'M5J2K3', 'M5E6F7', '지인 추천', true),
(6, 'hyunwoo.jung.m5', 'hyunwoo.jung.m5@algoga.local', '정현우', '010-2684-7901', '1993-09-09', 'MALE', '현우', 'M5L4M5', 'M5G8H9', '소셜 미디어', false),
(7, 'sumin.han.m5', 'sumin.han.m5@algoga.local', '한수민', '010-7815-4263', '1998-04-21', 'FEMALE', '수민', 'M5N6P7', NULL, '광고', true),
(8, 'jaewon.oh.m5', 'jaewon.oh.m5@algoga.local', '오재원', '010-6049-1557', '1990-12-02', 'MALE', '재원', 'M5Q8R9', 'M5N6P7', '지인 추천', true),
(9, 'dahye.shin.m5', 'dahye.shin.m5@algoga.local', '신다혜', '010-3395-6274', '1995-07-14', 'FEMALE', '다혜', 'M5S2T3', 'M5Q8R9', '검색 엔진', false),
(10, 'kihoon.yoon.m5', 'kihoon.yoon.m5@algoga.local', '윤기훈', '010-5527-9036', '1988-02-26', 'MALE', '기훈', 'M5U4V5', NULL, '광고', true),
(11, 'arin.baek.m5', 'arin.baek.m5@algoga.local', '백아린', '010-1973-4862', '1999-10-07', 'FEMALE', '아린', 'M5W6X7', 'M5U4V5', '소셜 미디어', true),
(12, 'jisoo.lim.m5', 'jisoo.lim.m5@algoga.local', '임지수', '010-8462-3159', '1992-05-31', 'OTHER', '지수', 'M5Y8Z9', 'M5W6X7', '지인 추천', false);

INSERT INTO users (
    username, email, password, name, phone, birth_date, gender, nickname, social_type,
    personal_code, login_fail_count, locked_until, previous_password, is_deleted, deleted_at,
    referral_code, signup_path, created_at, requires_password_change,
    terms_service_agreed, terms_privacy_agreed, terms_marketing_agreed,
    profile_image_url, selected_country_id, diagnosis_level, diagnosis_score, diagnosed_at
)
SELECT su.username, su.email, @user_password, su.name, su.phone, su.birth_date, su.gender, su.nickname, 'LOCAL',
       su.personal_code, 0, NULL, NULL, false, NULL,
       su.referral_code, su.signup_path, DATE_SUB(@now, INTERVAL (24 - su.seq) DAY), false,
       true, true, su.marketing_agreed,
       NULL,
       sc.country_id,
       CASE MOD(su.seq, 3) WHEN 0 THEN 'ADVANCED' WHEN 1 THEN 'BEGINNER' ELSE 'INTERMEDIATE' END,
       CASE MOD(su.seq, 3) WHEN 0 THEN 86 WHEN 1 THEN 38 ELSE 64 END,
       DATE_SUB(@now, INTERVAL (18 - su.seq) DAY)
FROM seed_users su
JOIN seed_courses sc ON sc.seq = ((su.seq - 1) MOD @course_count) + 1
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = su.email);

DROP TEMPORARY TABLE IF EXISTS seed_user_ids;
CREATE TEMPORARY TABLE seed_user_ids AS
SELECT su.seq, u.user_id, su.username, su.email, su.name, su.nickname, su.personal_code, su.referral_code
FROM seed_users su
JOIN users u ON u.email = su.email;

DROP TEMPORARY TABLE IF EXISTS seed_referrers;
CREATE TEMPORARY TABLE seed_referrers AS SELECT * FROM seed_user_ids;

DROP TEMPORARY TABLE IF EXISTS seed_referred_users;
CREATE TEMPORARY TABLE seed_referred_users AS SELECT * FROM seed_user_ids;

DROP TEMPORARY TABLE IF EXISTS seed_friend_requesters;
CREATE TEMPORARY TABLE seed_friend_requesters AS SELECT * FROM seed_user_ids;

DROP TEMPORARY TABLE IF EXISTS seed_friend_receivers;
CREATE TEMPORARY TABLE seed_friend_receivers AS SELECT * FROM seed_user_ids;

INSERT INTO referral_rewards (referrer_user_id, referred_user_id, reward_mileage, rewarded_at)
SELECT referrer.user_id, referred.user_id, 3000, DATE_SUB(@now, INTERVAL (20 - referred.seq) DAY)
FROM seed_referred_users referred
JOIN seed_referrers referrer ON referrer.personal_code = referred.referral_code
WHERE NOT EXISTS (
    SELECT 1 FROM referral_rewards rr WHERE rr.referred_user_id = referred.user_id
);

INSERT INTO mileage_histories (user_id, course_id, manager_id, amount, type, reason, created_at, expired_at)
SELECT referrer.user_id, NULL, NULL, 3000, 'EARN', '추천인 회원가입 보상',
       DATE_SUB(@now, INTERVAL (20 - referred.seq) DAY),
       DATE_ADD(DATE_SUB(@now, INTERVAL (20 - referred.seq) DAY), INTERVAL 1 YEAR)
FROM seed_referred_users referred
JOIN seed_referrers referrer ON referrer.personal_code = referred.referral_code
WHERE NOT EXISTS (
    SELECT 1
    FROM mileage_histories mh
    WHERE mh.user_id = referrer.user_id
      AND mh.reason = '추천인 회원가입 보상'
      AND mh.created_at = DATE_SUB(@now, INTERVAL (20 - referred.seq) DAY)
);

DROP TEMPORARY TABLE IF EXISTS seed_friend_edges;
CREATE TEMPORARY TABLE seed_friend_edges (
    requester_seq INT NOT NULL,
    receiver_seq INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    favorite BOOLEAN NOT NULL,
    days_ago INT NOT NULL,
    PRIMARY KEY (requester_seq, receiver_seq)
);

INSERT INTO seed_friend_edges VALUES
(1,2,'ACCEPTED',true,23),(1,3,'ACCEPTED',false,22),(2,4,'ACCEPTED',true,21),(3,5,'ACCEPTED',false,20),
(4,6,'ACCEPTED',true,19),(5,7,'ACCEPTED',false,18),(6,8,'ACCEPTED',false,17),(7,9,'ACCEPTED',true,16),
(8,10,'ACCEPTED',false,15),(9,11,'ACCEPTED',false,14),(10,12,'ACCEPTED',true,13),(2,7,'ACCEPTED',false,12),
(3,8,'ACCEPTED',true,11),(4,9,'ACCEPTED',false,10),(5,10,'ACCEPTED',true,9),
(11,1,'REQUESTED',false,4),(12,2,'REQUESTED',false,3),(6,1,'REQUESTED',false,2),
(1,12,'BLOCKED',false,6),(9,4,'BLOCKED',false,5);

INSERT INTO friend_relations (requester_id, receiver_id, status, favorite, created_at, updated_at)
SELECT r.user_id, v.user_id, e.status, e.favorite,
       DATE_SUB(@now, INTERVAL e.days_ago DAY),
       DATE_SUB(@now, INTERVAL GREATEST(e.days_ago - 1, 0) DAY)
FROM seed_friend_edges e
JOIN seed_friend_requesters r ON r.seq = e.requester_seq
JOIN seed_friend_receivers v ON v.seq = e.receiver_seq
WHERE NOT EXISTS (
    SELECT 1
    FROM friend_relations fr
    WHERE (fr.requester_id = r.user_id AND fr.receiver_id = v.user_id)
       OR (fr.requester_id = v.user_id AND fr.receiver_id = r.user_id)
);

-- Coupon policies and user coupons
INSERT INTO coupon_policies (lecture_id, manager_id, coupon_name, discount_type, discount_value, valid_days, is_active, created_at, updated_at)
SELECT sc.course_id, @manager_id,
       CASE MOD(sc.seq, 3)
           WHEN 1 THEN '첫 여행 준비 할인'
           WHEN 2 THEN '완강 감사 할인'
           ELSE '동행 예약 할인'
       END,
       'RATE',
       CASE MOD(sc.seq, 3) WHEN 1 THEN 10 WHEN 2 THEN 15 ELSE 12 END,
       30,
       true,
       DATE_SUB(@now, INTERVAL 25 DAY),
       DATE_SUB(@now, INTERVAL 25 DAY)
FROM seed_courses sc
WHERE NOT EXISTS (
    SELECT 1
    FROM coupon_policies cp
    WHERE cp.lecture_id = sc.course_id
      AND cp.coupon_name = CASE MOD(sc.seq, 3)
           WHEN 1 THEN '첫 여행 준비 할인'
           WHEN 2 THEN '완강 감사 할인'
           ELSE '동행 예약 할인'
       END
);

INSERT INTO user_coupons (
    user_id, lecture_id, coupon_policy_id, coupon_name, discount_type, discount_value,
    status, issued_at, expired_at, used_at
)
SELECT u.user_id, NULL, NULL, '웰컴쿠폰', 'RATE', 10,
       CASE WHEN u.seq IN (1,2,4,8,10) THEN 'USED' ELSE 'ISSUED' END,
       DATE_SUB(@now, INTERVAL (24 - u.seq) DAY),
       DATE_ADD(DATE_SUB(@now, INTERVAL (24 - u.seq) DAY), INTERVAL 1 MONTH),
       CASE WHEN u.seq IN (1,2,4,8,10) THEN DATE_SUB(@now, INTERVAL (18 - u.seq) DAY) ELSE NULL END
FROM seed_user_ids u
WHERE NOT EXISTS (
    SELECT 1
    FROM user_coupons uc
    WHERE uc.user_id = u.user_id
      AND uc.coupon_name = '웰컴쿠폰'
);

INSERT INTO user_coupons (
    user_id, lecture_id, coupon_policy_id, coupon_name, discount_type, discount_value,
    status, issued_at, expired_at, used_at
)
SELECT u.user_id, sc.course_id, cp.coupon_policy_id, cp.coupon_name, cp.discount_type, cp.discount_value,
       CASE WHEN u.seq IN (3,5,6,7,9,11) THEN 'ISSUED' ELSE 'USED' END,
       DATE_SUB(@now, INTERVAL (17 - u.seq MOD 5) DAY),
       DATE_ADD(DATE_SUB(@now, INTERVAL (17 - u.seq MOD 5) DAY), INTERVAL 1 MONTH),
       CASE WHEN u.seq IN (3,5,6,7,9,11) THEN NULL ELSE DATE_SUB(@now, INTERVAL (12 - u.seq MOD 5) DAY) END
FROM seed_user_ids u
JOIN seed_courses sc ON sc.seq = ((u.seq - 1) MOD @course_count) + 1
JOIN coupon_policies cp ON cp.lecture_id = sc.course_id
WHERE NOT EXISTS (
    SELECT 1
    FROM user_coupons uc
    WHERE uc.user_id = u.user_id
      AND uc.coupon_policy_id = cp.coupon_policy_id
);

-- Enrollment, progress, quiz, completion, rewards, reviews
DROP TEMPORARY TABLE IF EXISTS seed_enrollment_plan;
CREATE TEMPORARY TABLE seed_enrollment_plan (
    user_seq INT NOT NULL,
    course_seq INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    progress_percent INT NOT NULL,
    completed BOOLEAN NOT NULL,
    days_ago INT NOT NULL,
    PRIMARY KEY (user_seq, course_seq)
);

INSERT INTO seed_enrollment_plan VALUES
(1,1,'COMPLETED',100,true,21),(1,2,'COMPLETED',100,true,18),(1,3,'ENROLLED',72,false,8),
(2,1,'COMPLETED',100,true,20),(2,4,'ENROLLED',46,false,7),
(3,2,'COMPLETED',100,true,19),(3,5,'ENROLLED',58,false,6),
(4,3,'COMPLETED',100,true,18),(4,1,'COMPLETED',100,true,15),(4,6,'ENROLLED',37,false,5),
(5,4,'COMPLETED',100,true,17),(5,2,'ENROLLED',64,false,5),
(6,5,'COMPLETED',100,true,16),(6,7,'ENROLLED',28,false,4),
(7,6,'COMPLETED',100,true,15),(7,3,'ENROLLED',83,false,4),
(8,7,'COMPLETED',100,true,14),(8,4,'COMPLETED',100,true,11),
(9,8,'COMPLETED',100,true,13),(9,5,'ENROLLED',51,false,3),
(10,1,'COMPLETED',100,true,12),(10,6,'COMPLETED',100,true,9),
(11,2,'COMPLETED',100,true,11),(11,7,'ENROLLED',43,false,2),
(12,3,'COMPLETED',100,true,10),(12,8,'ENROLLED',35,false,2);

INSERT INTO enrollments (user_id, lecture_id, status, enrolled_at, completed_at, access_expires_at)
SELECT u.user_id, sc.course_id, p.status,
       DATE_SUB(@now, INTERVAL p.days_ago DAY),
       CASE WHEN p.completed THEN DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 4, 0) DAY) ELSE NULL END,
       DATE_ADD(DATE_SUB(@now, INTERVAL p.days_ago DAY), INTERVAL 6 MONTH)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
WHERE NOT EXISTS (
    SELECT 1 FROM enrollments e WHERE e.user_id = u.user_id AND e.lecture_id = sc.course_id
);

INSERT INTO learning_progresses (
    user_id, lecture_id, chapter_id, watched_seconds, progress_rate, is_completed, created_at, updated_at
)
SELECT u.user_id,
       sc.course_id,
       c.chapter_id,
       CASE
           WHEN p.completed THEN c.duration_seconds
           ELSE FLOOR(c.duration_seconds * p.progress_percent / 100)
       END,
       CASE WHEN p.completed THEN 100 ELSE p.progress_percent END,
       p.completed,
       DATE_SUB(@now, INTERVAL p.days_ago DAY),
       DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 2, 0) DAY)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
JOIN chapters c ON c.lecture_id = sc.course_id AND COALESCE(c.is_deleted, false) = false
WHERE NOT EXISTS (
    SELECT 1
    FROM learning_progresses lp
    WHERE lp.user_id = u.user_id
      AND lp.chapter_id = c.chapter_id
);

INSERT INTO quiz_submissions (user_id, lecture_id, total_count, correct_count, score, submitted_at)
SELECT u.user_id,
       sc.course_id,
       COUNT(q.quiz_id),
       CASE WHEN p.completed THEN COUNT(q.quiz_id) ELSE GREATEST(COUNT(q.quiz_id) - 1, 0) END,
       CASE WHEN COUNT(q.quiz_id) = 0 THEN 0
            ELSE ROUND((CASE WHEN p.completed THEN COUNT(q.quiz_id) ELSE GREATEST(COUNT(q.quiz_id) - 1, 0) END) * 100 / COUNT(q.quiz_id))
       END,
       DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 3, 0) DAY)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
JOIN quizzes q ON q.lecture_id = sc.course_id AND COALESCE(q.is_deleted, false) = false
WHERE p.progress_percent >= 35
  AND NOT EXISTS (
      SELECT 1 FROM quiz_submissions qs WHERE qs.user_id = u.user_id AND qs.lecture_id = sc.course_id
  )
GROUP BY u.user_id, sc.course_id, p.completed, p.days_ago;

INSERT INTO quiz_submission_answers (
    submission_id, quiz_id, question, option1, option2, option3, option4,
    selected_option, correct_option, is_correct, explanation
)
SELECT qs.submission_id,
       q.quiz_id,
       q.question,
       q.option1,
       q.option2,
       q.option3,
       q.option4,
       CASE
           WHEN p.completed THEN q.correct_option
           WHEN ROW_NUMBER() OVER (PARTITION BY qs.submission_id ORDER BY q.quiz_id) = 1
                THEN CASE WHEN q.correct_option = 1 THEN 2 ELSE 1 END
           ELSE q.correct_option
       END,
       q.correct_option,
       CASE
           WHEN p.completed THEN true
           WHEN ROW_NUMBER() OVER (PARTITION BY qs.submission_id ORDER BY q.quiz_id) = 1 THEN false
           ELSE true
       END,
       q.explanation
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
JOIN quiz_submissions qs ON qs.user_id = u.user_id AND qs.lecture_id = sc.course_id
JOIN quizzes q ON q.lecture_id = sc.course_id AND COALESCE(q.is_deleted, false) = false
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_submission_answers qsa WHERE qsa.submission_id = qs.submission_id
);

INSERT INTO course_completions (user_id, lecture_id, certificate_code, completed_at)
SELECT u.user_id,
       sc.course_id,
       CONCAT('ALG-2026-CERT-M5', LPAD(u.seq, 2, '0'), LPAD(sc.seq, 2, '0')),
       DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 4, 0) DAY)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
WHERE p.completed = true
  AND NOT EXISTS (
      SELECT 1 FROM course_completions cc WHERE cc.user_id = u.user_id AND cc.lecture_id = sc.course_id
  );

INSERT INTO mileage_histories (user_id, course_id, manager_id, amount, type, reason, created_at, expired_at)
SELECT u.user_id,
       sc.course_id,
       NULL,
       GREATEST(sc.reward_mileage, 1000),
       'EARN',
       '강의 수료 보상',
       DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 4, 0) DAY),
       DATE_ADD(DATE_SUB(@now, INTERVAL GREATEST(p.days_ago - 4, 0) DAY), INTERVAL 1 YEAR)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
WHERE p.completed = true
  AND NOT EXISTS (
      SELECT 1
      FROM mileage_histories mh
      WHERE mh.user_id = u.user_id
        AND mh.course_id = sc.course_id
        AND mh.reason = '강의 수료 보상'
  );

INSERT INTO course_rewards (user_id, lecture_id, issued_coupon_count, mileage_history_id, rewarded_at)
SELECT u.user_id,
       sc.course_id,
       (SELECT COUNT(*) FROM user_coupons uc WHERE uc.user_id = u.user_id AND uc.lecture_id = sc.course_id),
       mh.mileage_history_id,
       mh.created_at
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
JOIN mileage_histories mh ON mh.user_id = u.user_id AND mh.course_id = sc.course_id AND mh.reason = '강의 수료 보상'
WHERE p.completed = true
  AND NOT EXISTS (
      SELECT 1 FROM course_rewards cr WHERE cr.user_id = u.user_id AND cr.lecture_id = sc.course_id
  );

DROP TEMPORARY TABLE IF EXISTS seed_reviews;
CREATE TEMPORARY TABLE seed_reviews (
    user_seq INT NOT NULL,
    course_seq INT NOT NULL,
    rating INT NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (user_seq, course_seq)
);

INSERT INTO seed_reviews VALUES
(1,1,5,'처음 여행을 준비하면서 막막했는데 강의 순서가 좋아서 필요한 준비물을 하나씩 체크할 수 있었습니다. 예약까지 이어지는 흐름도 이해하기 쉬웠습니다.'),
(1,2,5,'현지에서 조심해야 할 부분을 실제 사례로 설명해줘서 기억에 오래 남았습니다. 자료가 깔끔해서 다시 보기 좋았습니다.'),
(2,1,4,'전체적으로 실전적입니다. 결제와 예약 전에 꼭 봐야 할 내용이 많아서 가족 여행 준비에 도움이 됐습니다.'),
(3,2,5,'강의 길이가 부담스럽지 않고 챕터별로 핵심이 분명했습니다. 초보자도 따라가기 쉬운 구성이었습니다.'),
(4,3,5,'여행 루트를 짜기 전에 보면 좋은 강의입니다. 예상 비용과 동선 판단에 도움이 많이 됐습니다.'),
(4,1,4,'이미 알고 있던 내용도 있었지만 놓치기 쉬운 체크리스트가 좋아서 만족합니다.'),
(5,4,5,'숙소와 이동 관련 설명이 현실적이라 바로 적용했습니다. 다음 여행도 같은 흐름으로 준비하려고 합니다.'),
(6,5,4,'퀴즈가 있어서 내용을 제대로 이해했는지 확인할 수 있었습니다. 설명이 과하지 않아 좋았습니다.'),
(7,6,5,'추천받아 들었는데 기대보다 훨씬 실용적이었습니다. 출국 전 불안감이 많이 줄었습니다.'),
(8,7,5,'패키지 예약 전에 확인해야 할 항목을 정리하기 좋았습니다. 매니저 응답도 빠르고 친절했습니다.'),
(8,4,4,'동행자와 같이 보기 좋았습니다. 할인 쿠폰까지 사용해서 만족감이 높았습니다.'),
(9,8,5,'초보자 입장에서 필요한 내용만 정리되어 있었습니다. 바로 예약까지 진행했습니다.'),
(10,1,5,'전체 과정이 자연스럽게 연결되어 있어서 강의, 퀴즈, 예약까지 한 번에 준비할 수 있었습니다.'),
(10,6,4,'중간중간 실제 예시가 많아 이해가 쉬웠습니다. 모바일로 보기에도 편했습니다.'),
(11,2,5,'추천 강의로 봤는데 제 상황에 잘 맞았습니다. 수료 후 제공되는 혜택도 만족스럽습니다.'),
(12,3,4,'처음엔 어렵게 느껴졌지만 설명을 따라가니 여행 계획이 훨씬 구체화됐습니다.');

INSERT INTO course_reviews (lecture_id, user_id, rating, content, is_deleted, deleted_at, created_at, updated_at)
SELECT sc.course_id, u.user_id, r.rating, r.content, false, NULL,
       DATE_SUB(@now, INTERVAL (8 + MOD(u.seq, 5)) DAY),
       DATE_SUB(@now, INTERVAL (8 + MOD(u.seq, 5)) DAY)
FROM seed_reviews r
JOIN seed_user_ids u ON u.seq = r.user_seq
JOIN seed_courses sc ON sc.seq = ((r.course_seq - 1) MOD @course_count) + 1
WHERE NOT EXISTS (
    SELECT 1 FROM course_reviews cr WHERE cr.user_id = u.user_id AND cr.lecture_id = sc.course_id
);

-- Course QnA and comments
DROP TEMPORARY TABLE IF EXISTS seed_qna_plan;
CREATE TEMPORARY TABLE seed_qna_plan (
    user_seq INT NOT NULL,
    course_seq INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT,
    status VARCHAR(20) NOT NULL,
    days_ago INT NOT NULL,
    PRIMARY KEY (user_seq, course_seq, title)
);

INSERT INTO seed_qna_plan VALUES
(1,1,'수료 후 쿠폰 사용 가능 시점 문의','수료 후 발급되는 쿠폰은 바로 예약 결제에서 사용할 수 있나요?','네, 수료 처리가 완료되고 쿠폰이 발급되면 유효기간 안에서 예약 결제 시 바로 사용할 수 있습니다.','ANSWERED',18),
(2,1,'퀴즈 재응시 가능 여부','퀴즈를 제출하면 다시 응시할 수 있는지 궁금합니다.','현재 강의 퀴즈는 1회 제출 기준으로 운영됩니다. 제출 전 답변을 충분히 확인해주세요.','ANSWERED',17),
(3,2,'챕터 순서대로 들어야 하나요','중간 챕터부터 먼저 봐도 되는지 궁금합니다.',NULL,'WAITING',6),
(4,3,'예약 전 확인 자료','예약 전에 다시 확인해야 할 자료가 따로 있을까요?','강의 자료의 체크리스트와 숙소 안내 문서를 함께 확인하시면 좋습니다.','ANSWERED',15),
(5,4,'동행자와 같이 수강','동행자도 같은 과정으로 강의를 같이 봐도 되나요?','학습 이력과 수료증은 계정별로 관리되므로 동행자도 별도 계정으로 수강하는 것을 권장합니다.','ANSWERED',14),
(6,5,'마일리지 적립 기준','수료 보상 마일리지는 언제 적립되나요?','진도와 퀴즈 조건을 충족해 수료가 완료되면 시스템에서 자동으로 적립됩니다.','ANSWERED',13),
(7,6,'레벨 선택 문의','처음 가는 여행지인데 중급 강의를 들어도 괜찮을까요?',NULL,'WAITING',5),
(8,7,'환불 요청 처리 시간','취소 후 환불 요청을 넣으면 보통 얼마나 걸리나요?','접수 후 담당자가 확인하며 상태가 요청, 검토, 승인, 완료 순서로 변경됩니다.','ANSWERED',12),
(9,8,'추천 강의 기준','진단평가 결과에 따라 추천 강의가 달라지는 기준이 궁금합니다.',NULL,'WAITING',4),
(10,1,'결제 수단 문의','카드 결제 외에 다른 결제 수단도 사용할 수 있나요?','결제 연동사에서 제공하는 수단 범위 안에서 카드와 간편결제를 사용할 수 있습니다.','ANSWERED',11),
(11,2,'친구 추천 보상','친구가 제 개인코드로 가입하면 보상은 바로 들어오나요?','가입이 완료되면 추천인에게 마일리지가 적립됩니다. 중복 보상은 1회만 처리됩니다.','ANSWERED',10),
(12,3,'수료증 확인 위치','수료증 번호는 어디에서 확인할 수 있나요?','내 강의 또는 수료 이력에서 발급된 수료증 코드를 확인할 수 있습니다.','ANSWERED',9);

INSERT INTO course_qnas (lecture_id, user_id, manager_id, title, question, answer, status, created_at, answered_at)
SELECT sc.course_id, u.user_id,
       CASE WHEN q.status = 'ANSWERED' THEN @manager_id ELSE NULL END,
       q.title, q.question, q.answer, q.status,
       DATE_SUB(@now, INTERVAL q.days_ago DAY),
       CASE WHEN q.status = 'ANSWERED' THEN DATE_SUB(@now, INTERVAL GREATEST(q.days_ago - 1, 0) DAY) ELSE NULL END
FROM seed_qna_plan q
JOIN seed_user_ids u ON u.seq = q.user_seq
JOIN seed_courses sc ON sc.seq = ((q.course_seq - 1) MOD @course_count) + 1
WHERE NOT EXISTS (
    SELECT 1
    FROM course_qnas cq
    WHERE cq.user_id = u.user_id
      AND cq.lecture_id = sc.course_id
      AND cq.title = q.title
);

INSERT INTO course_qna_comments (qna_id, parent_comment_id, user_id, manager_id, writer_type, content, is_deleted, created_at)
SELECT cq.qna_id, NULL, u.user_id, NULL, 'USER',
       '상세한 안내 부탁드립니다. 예약 전에 정확히 확인하고 진행하고 싶습니다.',
       false,
       DATE_ADD(cq.created_at, INTERVAL 2 HOUR)
FROM course_qnas cq
JOIN seed_user_ids u ON u.user_id = cq.user_id
WHERE cq.title IN (SELECT title FROM seed_qna_plan)
  AND NOT EXISTS (
      SELECT 1 FROM course_qna_comments c WHERE c.qna_id = cq.qna_id AND c.writer_type = 'USER'
  );

INSERT INTO course_qna_comments (qna_id, parent_comment_id, user_id, manager_id, writer_type, content, is_deleted, created_at)
SELECT cq.qna_id, c.comment_id, NULL, @manager_id, 'MANAGER',
       '문의 주신 내용은 담당 기준으로 확인했습니다. 추가로 궁금한 점이 있으면 같은 글의 댓글로 남겨주세요.',
       false,
       DATE_ADD(cq.created_at, INTERVAL 1 DAY)
FROM course_qnas cq
JOIN course_qna_comments c ON c.qna_id = cq.qna_id AND c.writer_type = 'USER'
WHERE cq.status = 'ANSWERED'
  AND cq.title IN (SELECT title FROM seed_qna_plan)
  AND NOT EXISTS (
      SELECT 1 FROM course_qna_comments mc WHERE mc.qna_id = cq.qna_id AND mc.writer_type = 'MANAGER'
  );

-- Diagnosis results
INSERT INTO diagnosis_results (user_id, country_id, correct_count, total_count, score, level, created_at)
SELECT u.user_id,
       sc.country_id,
       CASE MOD(u.seq, 3) WHEN 0 THEN 5 WHEN 1 THEN 2 ELSE 4 END,
       6,
       CASE MOD(u.seq, 3) WHEN 0 THEN 83 WHEN 1 THEN 33 ELSE 67 END,
       CASE MOD(u.seq, 3) WHEN 0 THEN 'ADVANCED' WHEN 1 THEN 'BEGINNER' ELSE 'INTERMEDIATE' END,
       DATE_SUB(@now, INTERVAL (16 - MOD(u.seq, 7)) DAY)
FROM seed_user_ids u
JOIN seed_courses sc ON sc.seq = ((u.seq - 1) MOD @course_count) + 1
WHERE NOT EXISTS (
    SELECT 1
    FROM diagnosis_results dr
    WHERE dr.user_id = u.user_id
      AND dr.created_at = DATE_SUB(@now, INTERVAL (16 - MOD(u.seq, 7)) DAY)
);

-- Payments for lectures
DROP TEMPORARY TABLE IF EXISTS seed_used_course_coupons;
CREATE TEMPORARY TABLE seed_used_course_coupons AS
SELECT user_id,
       lecture_id,
       MIN(user_coupon_id) AS user_coupon_id,
       MAX(discount_value) AS discount_value
FROM user_coupons
WHERE status = 'USED'
  AND lecture_id IS NOT NULL
GROUP BY user_id, lecture_id;

INSERT INTO payments (
    booking_id, course_id, user_id, payment_type, amount, used_mileage, used_coupon_id,
    status, idempotency_key, portone_payment_id, payment_method, user_name, created_at
)
SELECT NULL,
       sc.course_id,
       u.user_id,
       'LECTURE_ONLY',
       GREATEST(sc.price - COALESCE(uc.discount_value, 0) * sc.price / 100, 1000),
       CASE WHEN u.seq IN (4,8,10) THEN 1000 ELSE 0 END,
       uc.user_coupon_id,
       'SUCCESS',
       CONCAT('M5-LECTURE-', u.seq, '-', sc.seq),
       CONCAT('imp_m5_lecture_', u.seq, '_', sc.seq),
       CASE MOD(u.seq, 3) WHEN 0 THEN 'KAKAOPAY' WHEN 1 THEN 'CARD' ELSE 'TOSSPAY' END,
       u.name,
       DATE_SUB(@now, INTERVAL p.days_ago DAY)
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
LEFT JOIN seed_used_course_coupons uc ON uc.user_id = u.user_id AND uc.lecture_id = sc.course_id
WHERE NOT EXISTS (
    SELECT 1 FROM payments pay WHERE pay.idempotency_key = CONCAT('M5-LECTURE-', u.seq, '-', sc.seq)
);

INSERT INTO mileage_histories (user_id, course_id, manager_id, amount, type, reason, created_at, expired_at)
SELECT u.user_id, sc.course_id, NULL, 1000, 'USE', '강의 결제 마일리지 사용',
       DATE_SUB(@now, INTERVAL p.days_ago DAY), NULL
FROM seed_enrollment_plan p
JOIN seed_user_ids u ON u.seq = p.user_seq
JOIN seed_courses sc ON sc.seq = ((p.course_seq - 1) MOD @course_count) + 1
WHERE u.seq IN (4,8,10)
  AND NOT EXISTS (
      SELECT 1
      FROM mileage_histories mh
      WHERE mh.user_id = u.user_id
        AND mh.course_id = sc.course_id
        AND mh.reason = '강의 결제 마일리지 사용'
  );

-- Bookings, booking payments, refunds
DROP TEMPORARY TABLE IF EXISTS seed_booking_plan;
CREATE TEMPORARY TABLE seed_booking_plan (
    seq INT PRIMARY KEY,
    user_seq INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    total_price INT NOT NULL,
    deposit_price INT NOT NULL,
    balance_price INT NOT NULL,
    nights INT NOT NULL,
    check_in_offset INT NOT NULL,
    refund_status VARCHAR(30),
    refund_reason TEXT
);

INSERT INTO seed_booking_plan VALUES
(1,1,'FULL_PAID','FULL','SUCCESS',1280000,300000,980000,4,32,NULL,NULL),
(2,2,'DEPOSIT_PAID','DEPOSIT','SUCCESS',1560000,350000,1210000,5,45,NULL,NULL),
(3,3,'FULL_PAID','FULL','SUCCESS',940000,250000,690000,3,25,NULL,NULL),
(4,4,'CANCEL_REQUESTED','FULL','SUCCESS',1780000,400000,1380000,5,28,'REQUESTED','동행자의 일정이 갑자기 변경되어 여행을 취소해야 합니다.'),
(5,5,'CANCEL_REQUESTED','FULL','SUCCESS',1120000,300000,820000,4,18,'APPROVED','출발 전 건강 문제로 여행이 어려워져 환불을 요청했습니다.'),
(6,6,'FULL_PAID','FULL','SUCCESS',1360000,320000,1040000,4,39,NULL,NULL),
(7,7,'PENDING','DEPOSIT','FAILED',1480000,330000,1150000,5,52,NULL,NULL),
(8,8,'FULL_PAID','FULL','SUCCESS',990000,250000,740000,3,21,NULL,NULL),
(9,9,'CANCEL_REQUESTED','FULL','SUCCESS',1640000,360000,1280000,5,30,'UNDER_REVIEW','회사 일정과 겹쳐 출국이 어려워졌습니다. 환불 가능 여부 확인 부탁드립니다.'),
(10,10,'REFUNDED','FULL','REFUNDED',1210000,280000,930000,4,16,'APPROVED','개인 일정 변경으로 기존 예약을 유지하기 어려워 환불 요청합니다.'),
(11,11,'DEPOSIT_PAID','DEPOSIT','SUCCESS',1390000,300000,1090000,4,48,NULL,NULL),
(12,12,'FULL_PAID','FULL','SUCCESS',1530000,350000,1180000,5,34,NULL,NULL),
(13,1,'FULL_PAID','BALANCE','SUCCESS',1280000,300000,980000,4,32,NULL,NULL),
(14,5,'CANCEL_REQUESTED','DEPOSIT','SUCCESS',880000,200000,680000,3,42,'REJECTED','단순 변심으로 취소를 요청했습니다.'),
(15,8,'PENDING','DEPOSIT','FAILED',1020000,250000,770000,3,-2,NULL,NULL);

DROP TEMPORARY TABLE IF EXISTS seed_booking_refs;
CREATE TEMPORARY TABLE seed_booking_refs (
    seq INT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_country_id BIGINT NOT NULL,
    accommodation_id BIGINT NOT NULL,
    package_id BIGINT,
    country_id BIGINT NOT NULL
);

DROP TEMPORARY TABLE IF EXISTS seed_packages;
CREATE TEMPORARY TABLE seed_packages AS
SELECT ROW_NUMBER() OVER (ORDER BY ranked.package_id) AS seq,
       ranked.package_id,
       ranked.accommodation_id,
       ranked.country_id
FROM (
    SELECT p.package_id,
           p.accommodation_id,
           COALESCE(p.country_id, a.country_id) AS country_id,
           ROW_NUMBER() OVER (PARTITION BY COALESCE(p.country_id, a.country_id) ORDER BY p.package_id) AS country_rank
    FROM packages p
    JOIN accommodations a ON a.accommodation_id = p.accommodation_id
    WHERE COALESCE(p.country_id, a.country_id) IS NOT NULL
      AND a.price_per_night IS NOT NULL
      AND a.nights IS NOT NULL
) ranked
WHERE ranked.country_rank = 1;

DROP TEMPORARY TABLE IF EXISTS seed_accommodations;
CREATE TEMPORARY TABLE seed_accommodations AS
SELECT ROW_NUMBER() OVER (ORDER BY ranked.accommodation_id) AS seq,
       ranked.accommodation_id,
       ranked.country_id
FROM (
    SELECT a.accommodation_id,
           a.country_id,
           ROW_NUMBER() OVER (PARTITION BY a.country_id ORDER BY a.accommodation_id) AS country_rank
    FROM accommodations a
    WHERE a.country_id IS NOT NULL
      AND a.price_per_night IS NOT NULL
      AND a.nights IS NOT NULL
) ranked
WHERE ranked.country_rank = 1;

SET @package_count := (SELECT COUNT(*) FROM seed_packages);
SET @accommodation_count := (SELECT COUNT(*) FROM seed_accommodations);

INSERT INTO seed_booking_refs (seq, user_id, preferred_country_id, accommodation_id, package_id, country_id)
SELECT bp.seq,
       u.user_id,
       sc.country_id,
       COALESCE(p.accommodation_id, a.accommodation_id),
       p.package_id,
       COALESCE(p.country_id, a.country_id)
FROM seed_booking_plan bp
JOIN seed_user_ids u ON u.seq = bp.user_seq
JOIN seed_courses sc ON sc.seq = ((bp.seq - 1) MOD @course_count) + 1
LEFT JOIN seed_packages p ON p.country_id = sc.country_id
LEFT JOIN seed_accommodations a ON a.country_id = sc.country_id
WHERE COALESCE(p.accommodation_id, a.accommodation_id) IS NOT NULL;

INSERT INTO bookings (
    accommodation_id, package_id, user_id, status, total_price, deposit_price, balance_price,
    booking_number, flight_info, return_flight_info, passenger_info,
    check_in_date, check_out_date, nights, installment_allowed, created_at, updated_at
)
SELECT br.accommodation_id,
       br.package_id,
       br.user_id,
       bp.status,
       bp.total_price,
       bp.deposit_price,
       bp.balance_price,
       CONCAT('M5R202607', LPAD(bp.seq, 4, '0')),
       JSON_OBJECT('airline','대한항공','flightNo',CONCAT('KE', 700 + bp.seq),'departure','ICN','arrival','현지공항'),
       JSON_OBJECT('airline','대한항공','flightNo',CONCAT('KE', 800 + bp.seq),'departure','현지공항','arrival','ICN'),
       JSON_ARRAY(JSON_OBJECT('name', (SELECT name FROM seed_user_ids WHERE user_id = br.user_id), 'birthDate', '1994-03-12', 'passportNo', CONCAT('M5P', LPAD(bp.seq, 6, '0')))),
       DATE_ADD(CURRENT_DATE(), INTERVAL bp.check_in_offset DAY),
       DATE_ADD(CURRENT_DATE(), INTERVAL (bp.check_in_offset + bp.nights) DAY),
       bp.nights,
       bp.payment_type IN ('DEPOSIT','BALANCE'),
       DATE_SUB(@now, INTERVAL (16 - MOD(bp.seq, 9)) DAY),
       DATE_SUB(@now, INTERVAL (14 - MOD(bp.seq, 7)) DAY)
FROM seed_booking_plan bp
JOIN seed_booking_refs br ON br.seq = bp.seq
WHERE br.accommodation_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bookings b WHERE b.booking_number = CONCAT('M5R202607', LPAD(bp.seq, 4, '0'))
  );

INSERT INTO payments (
    booking_id, course_id, user_id, payment_type, amount, used_mileage, used_coupon_id,
    status, idempotency_key, portone_payment_id, payment_method, user_name, created_at
)
SELECT b.booking_id,
       NULL,
       br.user_id,
       bp.payment_type,
       CASE bp.payment_type
           WHEN 'DEPOSIT' THEN bp.deposit_price
           WHEN 'BALANCE' THEN bp.balance_price
           ELSE bp.total_price
       END,
       CASE WHEN bp.seq IN (1,5,8,12) THEN 2000 ELSE 0 END,
       NULL,
       bp.payment_status,
       CONCAT('M5-BOOKING-', bp.seq),
       CONCAT('imp_m5_booking_', bp.seq),
       CASE MOD(bp.seq, 3) WHEN 0 THEN 'KAKAOPAY' WHEN 1 THEN 'CARD' ELSE 'TOSSPAY' END,
       (SELECT name FROM seed_user_ids WHERE user_id = br.user_id),
       DATE_SUB(@now, INTERVAL (15 - MOD(bp.seq, 8)) DAY)
FROM seed_booking_plan bp
JOIN seed_booking_refs br ON br.seq = bp.seq
JOIN bookings b ON b.booking_number = CONCAT('M5R202607', LPAD(bp.seq, 4, '0'))
WHERE NOT EXISTS (
    SELECT 1 FROM payments p WHERE p.idempotency_key = CONCAT('M5-BOOKING-', bp.seq)
);

INSERT INTO mileage_histories (user_id, course_id, manager_id, amount, type, reason, created_at, expired_at)
SELECT br.user_id, NULL, NULL, 2000, 'USE', '여행 예약 결제 마일리지 사용',
       DATE_SUB(@now, INTERVAL (15 - MOD(bp.seq, 8)) DAY), NULL
FROM seed_booking_plan bp
JOIN seed_booking_refs br ON br.seq = bp.seq
WHERE bp.seq IN (1,5,8,12)
  AND NOT EXISTS (
      SELECT 1
      FROM mileage_histories mh
      WHERE mh.user_id = br.user_id
        AND mh.reason = '여행 예약 결제 마일리지 사용'
        AND mh.created_at = DATE_SUB(@now, INTERVAL (15 - MOD(bp.seq, 8)) DAY)
  );

INSERT INTO refund_requests (
    booking_id, payment_id, user_id, user_name, status, reason, reject_reason, amount, created_at, updated_at
)
SELECT b.booking_id,
       p.payment_id,
       br.user_id,
       p.user_name,
       bp.refund_status,
       bp.refund_reason,
       CASE WHEN bp.refund_status = 'REJECTED' THEN '예약금만 결제된 상태에서는 전액 환불 대상이 아닙니다. 약관을 다시 확인해주세요.' ELSE NULL END,
       p.amount,
       DATE_SUB(@now, INTERVAL (7 - MOD(bp.seq, 4)) DAY),
       DATE_SUB(@now, INTERVAL (5 - MOD(bp.seq, 3)) DAY)
FROM seed_booking_plan bp
JOIN seed_booking_refs br ON br.seq = bp.seq
JOIN bookings b ON b.booking_number = CONCAT('M5R202607', LPAD(bp.seq, 4, '0'))
JOIN payments p ON p.idempotency_key = CONCAT('M5-BOOKING-', bp.seq)
WHERE bp.refund_status IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM refund_requests rr WHERE rr.booking_id = b.booking_id
  );


-- Presentation safety repair: keep seeded rows compatible with production DB enum/stat screens.
-- Some deployed DB enum definitions do not include EXPIRED even though the Java enum does.
UPDATE bookings
SET status = 'PENDING'
WHERE status = ''
  AND booking_number LIKE 'M5R202607%';


-- Normalize existing signup_path aliases to frontend labels for admin statistics.
UPDATE users SET signup_path = '지인 추천'
WHERE signup_path = 'friend';

UPDATE users SET signup_path = '소셜 미디어'
WHERE signup_path = 'social';

-- Keep M5 signup paths aligned to the frontend select options only.
UPDATE users SET signup_path = '검색 엔진'
WHERE username IN ('minji.kang.m5', 'taehyun.kim.m5', 'dahye.shin.m5');

UPDATE users SET signup_path = '지인 추천'
WHERE username IN ('junho.lee.m5', 'yuna.choi.m5', 'jaewon.oh.m5', 'jisoo.lim.m5');

UPDATE users SET signup_path = '소셜 미디어'
WHERE username IN ('seoyeon.park.m5', 'hyunwoo.jung.m5', 'arin.baek.m5');

UPDATE users SET signup_path = '광고'
WHERE username IN ('sumin.han.m5', 'kihoon.yoon.m5');

-- Remove only impossible M5 completion rows: completion without an enrollment for the same user/course.
DELETE cc
FROM course_completions cc
JOIN users u ON u.user_id = cc.user_id
LEFT JOIN enrollments e ON e.user_id = cc.user_id AND e.lecture_id = cc.lecture_id
WHERE u.username LIKE '%.m5'
  AND e.enrollment_id IS NULL;

-- If the seed was interrupted/re-run before idempotency guards, keep only one M5 completion per user/course.
DROP TEMPORARY TABLE IF EXISTS seed_duplicate_completion_keep;
CREATE TEMPORARY TABLE seed_duplicate_completion_keep AS
SELECT MIN(cc.completion_id) AS keep_id
FROM course_completions cc
JOIN users u ON u.user_id = cc.user_id
WHERE u.username LIKE '%.m5'
GROUP BY cc.user_id, cc.lecture_id;

DELETE cc
FROM course_completions cc
JOIN users u ON u.user_id = cc.user_id
LEFT JOIN seed_duplicate_completion_keep keepers ON keepers.keep_id = cc.completion_id
WHERE u.username LIKE '%.m5'
  AND keepers.keep_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS seed_duplicate_completion_keep;

COMMIT;


