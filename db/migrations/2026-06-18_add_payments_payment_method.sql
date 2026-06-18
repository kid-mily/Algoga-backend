-- 결제수단(실제 PG 수단: TOSSPAY/KAKAOPAY/PaymentMethodCard 등) 저장용 컬럼 추가
-- 배경: 어드민 결제내역 조회의 '결제수단' 컬럼에 결제'유형'(LECTURE_ONLY)이 잘못 노출되던 버그 수정.
--       PortOne 응답의 method를 추출해 이 컬럼에 저장하고, 어드민/내 결제내역 응답에서 사용한다.
-- 기존 행은 NULL (과거 결제는 수단 미저장) → 응답에서 '-' 로 표시됨.
-- ddl-auto: update 가 컬럼을 자동 추가하긴 하지만, 운영 DB 명시 적용용으로 남겨둠.

ALTER TABLE payments ADD COLUMN payment_method VARCHAR(30) NULL;
