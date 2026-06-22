-- 결제/환불 내역에 '사용자명 스냅샷' 컬럼 추가
-- 배경: 삭제 정책상 결제/예약/환불은 잔여 데이터로 유지되지만, user 정보는 비활성화 14일 후 hard delete 됨.
--       어드민 결제/환불 내역은 사용자명을 user 테이블에서 live 조회하므로, user 가 삭제되면 이름이 사라짐.
--       → 결제/환불 시점의 사용자명을 박제(스냅샷)해 잔여 내역에서도 이름이 보이도록 함 (결제수단 스냅샷과 동일 방식).
-- 기존 행은 NULL → 조회 시 user 테이블 live 조회로 fallback (user 가 살아있는 동안만 표시).
-- ddl-auto: update 가 컬럼을 자동 추가하므로 운영 적용은 보통 불필요 (명시용으로 보관).

ALTER TABLE payments        ADD COLUMN user_name VARCHAR(50) NULL;
ALTER TABLE refund_requests ADD COLUMN user_name VARCHAR(50) NULL;
