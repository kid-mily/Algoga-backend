// k6/scripts/02-mixed-shop-traffic.js
// ------------------------------------------------------------
// 목적:
// - 조회 중심 쇼핑몰 트래픽을 단순화해 재현합니다.
// - 80% 사용자는 인기 상품을 조회하고, 20% 사용자는 주문을 생성합니다.
//
// 왜 필요한가:
// - 단일 API 테스트는 기준선을 만들기 좋습니다.
// - 하지만 실제 운영에서는 조회와 쓰기가 동시에 발생합니다.
// - 주문 생성은 DB write, 재고 감소, 결제 지연을 포함하므로 조회 API와 다른 병목을 만들 수 있습니다.
//
// 실행:
//   k6 run .\k6\scripts\02-mixed-shop-traffic.js
// ------------------------------------------------------------

import http from 'k6/http';
import { sleep } from 'k6';
import { createOrder, getPopularProducts } from '../lib/client.js';
import { randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

// 현재 실습에서는 재고 부족으로 인한 400을 "관찰 가능한 비즈니스 실패"로 다룬다.
// k6의 http_req_failed는 기본적으로 4xx를 실패로 볼 수 있으므로,
// 이 시나리오에서는 400을 기대 가능한 응답으로 포함한다.
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }, 400));

export const options = {
    // k6 콘솔 요약에 p99를 포함합니다.
    // 혼합 트래픽에서는 일부 주문 요청이나 느린 조회 요청이 tail latency로 나타날 수 있습니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    scenarios: {
        mixed_shop_traffic: {
            // ramping-vus는 사용자 수 증가에 따른 시스템 변화를 보기 위한 executor입니다.
            // 이 시나리오는 "조회와 주문이 동시에 증가할 때 어떤 API가 먼저 느려지는가"를 관찰합니다.
            executor: 'ramping-vus',

            // target은 해당 구간이 끝날 때 도달할 VU 수입니다.
            // 10명 -> 30명 -> 50명으로 늘리면서 부하가 커질 때의 응답 시간과 오류율을 봅니다.
            stages: [
                { duration: '30s', target: 10 },
                { duration: '1m', target: 30 },
                { duration: '1m', target: 50 },
                { duration: '30s', target: 0 },
            ],

            // ramp-down 중 진행 중인 요청이 자연스럽게 끝날 시간을 줍니다.
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        // 혼합 부하에서는 재고 부족 등 비즈니스 실패가 섞일 수 있으므로 baseline보다 여유 있게 둡니다.
        http_req_failed: ['rate<0.05'],

        // 인기 상품 조회는 Stage 4의 핵심 관찰 대상입니다.
        // 혼합 트래픽에서는 주문 write 부하와 경쟁하므로 baseline보다 완화된 기준을 둡니다.
        'http_req_duration{type:popular}': ['p(95)<700'],

        // 주문 생성은 결제 지연, 재고 감소, DB write가 포함되어 조회보다 느릴 수 있습니다.
        // API 성격별로 threshold를 다르게 잡아야 해석이 정확합니다.
        'http_req_duration{type:order}': ['p(95)<1200'],
    },
};

export default function () {
    const traffic = Math.random();

    if (traffic < 0.8) {
        // 80%: 사용자는 인기 상품 목록을 확인합니다.
        getPopularProducts();
    } else {
        // 20%: 사용자는 인기 상품 중 하나를 주문한다고 가정합니다.
        createOrder();
    }

    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('02-mixed-shop-traffic');