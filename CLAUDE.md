# ALGOGA 프로젝트 컨텍스트

## 프로젝트 개요
- **프로젝트명**: algoga_server
- **스택**: Spring Boot 3.5.14 / Java 17 / Gradle
- **서버 포트**: 15000
- **DB**: MySQL (algoga/algoga @ localhost:3306)
- **Redis**: localhost:6379
- **파일 스토리지**: MinIO (S3 호환)
- **결제 PG**: PortOne API

## 아키텍처
- **패턴**: DDD + Hexagonal Architecture (포트-어댑터)
- **계층**: presentation → application → domain → infrastructure
- **이벤트**: Spring ApplicationEventPublisher 기반 도메인 이벤트
- **인증**: JWT (Access 30분 / Refresh 7일) + BCrypt

## 내 담당 도메인 (핵심)
> **booking / payment / refund** 3개 도메인 담당

### Booking (예약)
- 상태: `PENDING → DEPOSIT_PAID → FULL_PAID → CANCEL_REQUESTED → REFUNDED`
- 예약번호 형식: `BK-yyyyMMdd-nnnnn`
- 금액: 총액의 30% 선금 / 70% 잔금
- 이벤트 발행: `BookingCreatedEvent`, `BookingCanceledEvent`

### Payment (결제)
- 상태: `SUCCESS / FAILED / REFUNDED`
- 유형: `DEPOSIT / BALANCE / FULL / LECTURE_ONLY`
- **외부 API**: PortOne `GET /payments/{id}` — 결제 검증
- **중복 방지**: idempotencyKey = `{bookingId}_{paymentType}`
- 쿠폰(PERCENT/FIXED) + 마일리지 차감 지원
- 웹훅 처리: PortOne → `/api/v1/payments/webhook`
- 이벤트 발행: `PaymentCompletedEvent`, `PackagePaymentCompletedEvent`
- 어드민: 기간별 조회, Excel export, 월별 수익 통계

### Refund (환불)
- 상태: `REQUESTED → APPROVED → COMPLETED` (또는 `REJECTED`)
- 환불 정책: 체크인 14일 전 100% / 7일 전 50% / 7일 미만 0%
- **외부 API**: PortOne `POST /payments/{id}/cancel` — 실제 환불
- 이벤트 발행: `RefundApprovedEvent`, `RefundRejectedEvent`

## 현재 성능 최적화 작업 진행 중

### 목표
- Resilience4j 서킷브레이커 + Redis TTL 캐시 도입
- Prometheus + Grafana로 인프라 모니터링
- 관리자 페이지에서 대시보드 시각화

### 작업 순서
1. **PortOne API 호출을 트랜잭션 밖으로 분리** ← 현재 여기부터 시작 예정
2. Resilience4j 서킷브레이커 (PortOneClient에 적용)
3. Spring Cache + Redis (조회성 API 캐싱)
4. Spring Actuator + Prometheus 메트릭 노출
5. Grafana 대시보드 (Docker Compose)
6. 관리자 페이지 연동

### 문제점 (파악 완료)
- **핵심 구조 문제**: `PaymentCommandService.handle()`, `RefundCommandService.complete()` 에서
  PortOne API 호출이 `@Transactional` 내부에 있음
  → PortOne 느리거나 실패 시 DB 커넥션 오래 점유, 전체 롤백 발생
- Redis는 설정만 있고 이메일 인증 코드에만 수동 사용 중 (Spring Cache 추상화 없음)
- 서킷브레이커 없음 → PortOne, 항공편 API(data.go.kr) 호출 무방비

### 캐시 전략 (도메인별 TTL)
| 대상 | TTL | 이유 |
|------|-----|------|
| 월별 결제 통계 (`getAdminPaymentStats`) | 1시간 | 집계 쿼리 비용 높음 |
| 내 예약 목록 (`getMyBookings`) | 5분 | 쓰기 발생 시 evict |
| 내 결제 목록 (`getMyPayments`) | 5분 | 쓰기 발생 시 evict |
| 숙소 기본 정보 (타 도메인) | 24시간 | 변경 빈도 낮음 |

### Grafana 모니터링 대상
- 서킷브레이커 상태 (CLOSED / OPEN / HALF_OPEN)
- PortOne API 응답 시간
- 결제 성공/실패율
- 월별 통계 API 쿼리 시간
- JVM, DB 커넥션 풀

## 주요 파일 경로
```
src/main/java/com/kidmily/algoga_server/
├── global/
│   ├── config/          # RedisConfig, AsyncConfig, SecurityConfig 등
│   ├── jwt/             # GlobalJwtProvider, GlobalJwtAuthenticationFilter
│   └── exception/       # GlobalExceptionHandler, BusinessException
├── booking/
│   ├── domain/model/    # Booking.java, BookingStatus.java
│   ├── application/service/ # BookingCommandService, BookingQueryService
│   └── infrastructure/portone/ # (없음 - 외부 API 없음)
├── payment/
│   ├── domain/model/    # Payment.java, PaymentStatus.java, PaymentType.java
│   ├── application/service/ # PaymentCommandService, PaymentQueryService
│   └── infrastructure/portone/ # PortOneClient.java ← 서킷브레이커 적용 대상
└── refund/
    ├── domain/model/    # RefundRequest.java, RefundStatus.java
    ├── application/service/ # RefundCommandService, RefundQueryService
    └── (PortOne은 PaymentDomain의 PortOneClient 재사용)
```

## 기타 도메인 (참고)
accommodation, user, lms(강의/수강), community, notification, benefit(쿠폰/마일리지), friend, banner, flight, calendar, notice, admin

## 협업 규칙
- 파일 생성 / 커밋 / 푸시는 사용자가 직접 진행
- 코드는 제시만 하거나 파일을 직접 수정
- 개발 중 CLAUDE.md 업데이트 필요한 사항이 생기면 즉시 반영
- 각 STEP 개발 완료 후 변경 내용 요약 제공 (변경 파일, 변경 이유, 테스트 방법)

## 빌드 및 실행 명령어
```bash
./gradlew build       # 빌드
./gradlew test        # 테스트
./gradlew bootRun     # 서버 실행 (포트 15000)
```
