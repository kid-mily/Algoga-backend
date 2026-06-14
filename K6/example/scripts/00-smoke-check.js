// k6/scripts/00-smoke-check.js
// ------------------------------------------------------------
// 목적:
// - 본격적인 부하테스트 전에 애플리케이션이 정상 응답하는지 확인합니다.
// - smoke test는 성능 한계를 찾는 테스트가 아니라 "테스트 가능한 상태인지" 확인하는 테스트입니다.
//
// 실행:
//   k6 run .\k6\scripts\00-smoke-check.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { createOrder, getPopularProducts, getProduct } from '../lib/client.js';
import { randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    // vus는 Virtual Users의 약자입니다.
    // smoke test에서는 성능 한계를 보려는 것이 아니라 API가 정상 동작하는지만 확인하므로 1명으로 충분합니다.
    vus: 1,

    // iterations는 default function을 총 몇 번 실행할지 정합니다.
    // 여기서는 1명의 VU가 상품 조회, 인기 상품 조회, 주문 생성을 묶어서 3번 반복합니다.
    // duration 대신 iterations를 쓰면 테스트 실행 횟수가 고정되어 결과가 예측 가능합니다.
    iterations: 3,

    // summaryTrendStats는 k6 콘솔 요약에 어떤 통계값을 보여줄지 정합니다.
    // k6 기본 출력은 p90/p95 중심이라 p99가 안 보일 수 있으므로 수업에서는 p99를 명시적으로 추가합니다.
    // p95는 대부분의 사용자 경험을 보는 기준이고, p99는 극단적으로 느린 tail latency를 보는 기준입니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    thresholds: {
        // http_req_failed는 HTTP 요청 실패율입니다.
        // smoke test에서 실패율이 1% 이상이면 부하테스트를 진행하기 전에 환경부터 확인해야 합니다.
        http_req_failed: ['rate<0.01'],

        // http_req_duration은 클라이언트가 본 전체 응답 시간입니다.
        // smoke test 기준은 엄격한 성능 목표가 아니라 "명백히 비정상적으로 느리지 않은가"를 확인하는 용도입니다.
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    // 1. 단순 상품 조회로 API 서버와 DB 연결을 확인합니다.
    getProduct(1);

    // 2. 인기 상품 조회로 Stage 4의 핵심 대상 API를 확인합니다.
    getPopularProducts();

    // 3. 주문 생성으로 쓰기 API와 주문 로그/메트릭을 확인합니다.
    createOrder(1, 1);

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('00-smoke-check');