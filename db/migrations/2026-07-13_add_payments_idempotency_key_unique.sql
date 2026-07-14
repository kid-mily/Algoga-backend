-- payments.idempotency_key 에 UNIQUE 인덱스 추가 (중복 결제 방어 강화)
--
-- 배경:
--   idempotency_key(= "{bookingId}_{paymentType}" 또는 "LECTURE_{courseId}_{userId}")로 중복 결제를 막고 있으나,
--   DB에 UNIQUE 제약이 없어서 "이전 시도가 FAILED로 남은 뒤 재시도"가 반복되면 같은 키의 행이 누적되고,
--   이후 findByIdempotencyKey 가 NonUniqueResultException(500)을 던질 수 있었다.
--   애플리케이션에서는 FAILED 행을 지우고 새로 기록하도록 수정했고, DB 차원의 안전장치로 UNIQUE 인덱스를 건다.
--
-- 주의:
--   ddl-auto: update 는 "기존 컬럼에 UNIQUE 제약 추가"를 신뢰성 있게 적용하지 못한다(신규 컬럼/테이블만 자동 반영).
--   따라서 운영 DB에는 이 스크립트를 명시적으로 실행할 것. (로컬/신규 스키마는 엔티티 @UniqueConstraint 로 자동 반영)
--
-- 실행 순서: (1) 혹시 남아있는 중복 정리 → (2) UNIQUE 인덱스 추가

-- (1) 중복 idempotency_key 정리: 키별로 한 행만 남긴다.
--     우선순위 = SUCCESS 행 우선, 그 다음 최신(payment_id 큰 것). 나머지(과거 FAILED 잔여분)는 삭제.
DELETE p FROM payments p
JOIN (
    SELECT idempotency_key,
           SUBSTRING_INDEX(
               GROUP_CONCAT(payment_id ORDER BY (status = 'SUCCESS') DESC, payment_id DESC),
               ',', 1
           ) AS keep_id
    FROM payments
    GROUP BY idempotency_key
    HAVING COUNT(*) > 1
) k ON p.idempotency_key = k.idempotency_key
WHERE p.payment_id <> k.keep_id;

-- (2) UNIQUE 인덱스 추가. 이미 존재하면(엔티티로 이미 생성된 경우) 에러가 나므로 그때는 이 문장을 건너뛴다.
ALTER TABLE payments
    ADD UNIQUE INDEX uk_payments_idempotency_key (idempotency_key);
