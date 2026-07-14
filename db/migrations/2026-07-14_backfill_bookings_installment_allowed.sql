-- bookings.installment_allowed NULL 백필
--
-- 배경:
--   패키지 결제정책(PR #456, 2026-07-10)에서 bookings.installment_allowed 컬럼이 추가됐는데,
--   ddl-auto: update 가 컬럼을 nullable 로 자동 추가하면서 그 이전 예약들은 값이 전부 NULL 로 남았다.
--   엔티티가 primitive boolean 이던 시점엔 이 NULL 행을 조회하는 순간
--   "Can not set boolean field BookingJpaEntity.installmentAllowed to null value" (JpaSystemException) → 500.
--   → 통계매니저 '돈 요약'(overview)에서 넓은 기간(과거 예약 포함) 조회 시 500 발생.
--
-- 조치:
--   1) 엔티티 필드를 Boolean(래퍼)로 변경 + 매퍼에서 NULL→true 처리 (재발 방지, 코드)
--   2) 아래 백필로 기존 NULL 을 정리 (과거 예약은 전부 일반 예약 = 분할 허용이므로 TRUE)
--
--   운영 DB에 아래 UPDATE 를 1회 실행하면 즉시 500 이 사라진다.

UPDATE bookings SET installment_allowed = TRUE WHERE installment_allowed IS NULL;
