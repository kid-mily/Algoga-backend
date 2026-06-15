// k6/lib/config.js
// ------------------------------------------------------------
// k6 실습 스크립트에서 공통으로 사용하는 설정 모음입니다.
//
// 수업 의도:
// - 각 스크립트마다 URL, sleep 시간, 상품 ID 범위를 중복 작성하지 않는다.
// - 환경 변수로 테스트 조건을 바꾸는 습관을 만든다.
// - 부하테스트 스크립트도 운영 코드처럼 읽기 쉽고 변경하기 쉬워야 한다.
// ------------------------------------------------------------

// BASE_URL은 테스트 대상 Spring Boot 애플리케이션 주소입니다.
// PowerShell 예시:
//   $env:BASE_URL='http://localhost:8080'
//   k6 run .\k6\scripts\01-popular-baseline.js
//  환경 변수 BASE_URL이 있으면 그 값을 사용하고,
// 없으면 기본값 http://localhost:8080을 사용한다.
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:15000';

// 테스트 데이터의 productId 범위입니다.
// 현재 data.sql에는 1~7번 상품이 들어 있습니다.
export const PRODUCT_ID_MIN = Number(__ENV.PRODUCT_ID_MIN || 1);
export const PRODUCT_ID_MAX = Number(__ENV.PRODUCT_ID_MAX || 7);

// 사용자가 요청 사이에 쉬는 시간을 단순화한 값입니다.
// 실제 사용자는 API를 초당 무한히 호출하지 않으므로 sleep이 필요합니다.
export const MIN_SLEEP_SECONDS = Number(__ENV.MIN_SLEEP_SECONDS || 1);
export const MAX_SLEEP_SECONDS = Number(__ENV.MAX_SLEEP_SECONDS || 3);

// 주문 수량 기본값입니다.
// 큰 값을 사용하면 재고 부족 실패를 의도적으로 만들 수 있습니다.
export const ORDER_QUANTITY = Number(__ENV.ORDER_QUANTITY || 1);

// 결과 파일 이름 prefix입니다.
// 여러 번 실행할 때 덮어쓰기를 피하려면 RESULT_NAME을 바꿉니다.
// 예시:
//   $env:RESULT_NAME='baseline-before-index'
export const RESULT_NAME = __ENV.RESULT_NAME || 'k6-result';

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function randomProductId() {
    return randomInt(PRODUCT_ID_MIN, PRODUCT_ID_MAX);
}

export function randomSleepSeconds() {
    return Math.random() * (MAX_SLEEP_SECONDS - MIN_SLEEP_SECONDS) + MIN_SLEEP_SECONDS;
}