# 프론트엔드 이슈 정리

## ✅ 해결 완료 — 단과 결제 500 에러 (2026-06-16)

### 현상
- 단과 결제 (`POST /api/v1/payments/lecture`) 시도 시 500 반환
- 에러 코드: `GLOBAL_001` — 서버 내부에서 오류가 발생했습니다.
- traceId: `39939a9a`, `835ca31a`, `837f85fc`, `f27a6541`, `cc1d1702`

### 원인 (2가지 복합)

**1. Spring self-invocation 으로 인한 `@Transactional` 무시**
- `PaymentCommandService.handleLecturePayment()` 가 같은 클래스의 `saveLecturePayment()`(`@Transactional`)를
  `this.method()` 형태로 직접 호출 → AOP 프록시를 거치지 않아 트랜잭션이 전혀 적용되지 않음
- 트랜잭션(세션) 없이 `courseRepository.findByIdAndDeletedFalse()`가 lazy 컬렉션(`Course.chapters`)을 읽으려다
  `LazyInitializationException` 발생
- 같은 패턴이 `handle()→savePayment()`, `handleWebhook()→processWebhook()` 에도 있었음 (캐시 무효화 `@CacheEvict`도 같이 무시되고 있던 숨은 버그)

**2. DB 스키마 불일치**
- `payments.booking_id` 컬럼이 실제 DB에는 `NOT NULL`로 박혀있었으나, JPA 엔티티는 `nullable = true`로 정의
- `ddl-auto: update` 는 기존 컬럼 제약을 자동으로 완화해주지 않아서 발생한 불일치
- 강의 단독결제(`LECTURE_ONLY`)는 예약이 없어 `bookingId = null`로 저장하는데, DB가 이를 막아서
  `DataIntegrityViolationException` 발생

### 해결
1. `PaymentTransactionService` 신규 빈 분리 — `@Transactional`/`@CacheEvict` 메서드들을 별도 빈으로 옮겨
   `PaymentCommandService`가 이 빈을 주입받아 호출하도록 변경 (self-invocation 제거)
2. `db/migrations/2026-06-16_fix_payments_booking_id_nullable.sql` — `booking_id` 컬럼 NULL 허용으로 수정
   - **로컬 DB 적용 완료**
   - **⚠️ 운영 DB(kidmily.kro.kr) 미적용 — 배포 시 반드시 동일 마이그레이션 실행 필요**

### 검증
- Swagger `POST /api/v1/payments/lecture` 로 직접 테스트 — 201 `LECTURE_PAYMENT_CREATED` 확인 (paymentId: 7)

---

## ✅ 완료 — 피그마 수정 요청 전달 완료

### 정산매니저 - 결제 내역 조회
- 기간 필터 추가 (from ~ to 날짜 선택) → 전달 완료

### 통계매니저 - 예약 전환율 분석
1. 방문자 수 카드 제거 → 카드 3개로 변경
2. "예약 시도 수" 라벨 → "결제 페이지 진입 수" 로 변경
3. 기간 필터 추가 (프리셋 버튼 + 직접 날짜 입력)
4. 상품별 차트 Top N 필터 추가
5. 상위/하위 상품 더보기 버튼 추가
6. 상품별 차트 다운로드 버튼 추가

### 통계매니저 - 나라별 인기도 분석
1. 방문자 수 컬럼 제거 → 테이블 5개 컬럼 (국가 / 회원가입 수 / 예약 건수 / 매출 / 점유율)
2. 기간 필터 추가 (프리셋 버튼 + 직접 날짜 입력) — 페이지 상단
3. 테이블 컬럼 정렬 기능 추가 (↕) — 기본 정렬: 매출 내림차순
4. 테이블 페이지네이션 추가 — 10개씩
5. Top 10 차트 — Top N 필터 추가 (Top 5 / 10 / 15 / 20)
6. 다운로드 버튼 위치/디자인 통일
7. 검색창 동작 방식 명시 요청 (테이블만 필터링 or 차트도 같이)
