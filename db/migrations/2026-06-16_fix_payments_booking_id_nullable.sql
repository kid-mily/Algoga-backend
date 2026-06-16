-- 2026-06-16
-- 이슈: 단과(강의 단독) 결제 시 500 에러 (GLOBAL_001)
-- 원인: payments.booking_id 컬럼이 실제 DB에는 NOT NULL로 박혀있었으나,
--       JPA 엔티티(PaymentJpaEntity)는 nullable=true로 정의되어 있어 스키마 불일치 발생.
--       ddl-auto: update 는 기존 컬럼의 제약조건을 자동으로 완화해주지 않기 때문에
--       엔티티만 보고는 문제를 알 수 없었음.
--       LECTURE_ONLY 결제(강의 단독결제, 예약 없음)는 bookingId = null 로 저장되는데
--       DB가 NOT NULL을 강제해서 insert 시점에 DataIntegrityViolationException 발생.
--
-- 적용 대상: 로컬 DB 적용 완료 (2026-06-16)
--           운영 DB(kidmily.kro.kr) 미적용 — 배포 서버 DB에도 반드시 동일하게 실행 필요!
--
-- 실행 방법:
--   mysql -h <host> -P 3306 -u algoga -p algoga < 2026-06-16_fix_payments_booking_id_nullable.sql

ALTER TABLE payments MODIFY booking_id BIGINT NULL;
