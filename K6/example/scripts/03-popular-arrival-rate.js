// k6/scripts/03-popular-arrival-rate.js
// ------------------------------------------------------------
// 목적:
// - VU 수가 아니라 초당 요청 시작 수를 기준으로 부하를 만듭니다.
// - "초당 30개 인기 상품 조회 요청을 처리할 수 있는가?"처럼 RPS 목표가 있을 때 사용합니다.
//
// ramping-vus와 차이:
// - ramping-vus: 동시 사용자 수를 늘립니다.
// - constant-arrival-rate: 요청 시작률을 일정하게 유지하려고 합니다.
//
// 실행:
//   k6 run .\k6\scripts\03-popular-arrival-rate.js
//
// 요청률 변경:
//   $env:POPULAR_RATE='50'
//   k6 run .\k6\scripts\03-popular-arrival-rate.js
// ------------------------------------------------------------

import { getPopularProducts } from '../lib/client.js';
import { createSummaryHandler } from '../lib/summary.js';

const POPULAR_RATE = Number(__ENV.POPULAR_RATE || 30);

export const options = {
    // arrival-rate 테스트에서도 p99를 콘솔 요약에 포함합니다.
    // 목표 RPS를 유지하는 동안 극단적으로 느린 요청이 생기는지 확인하기 위함입니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    scenarios: {
        popular_fixed_rate: {
            // constant-arrival-rate는 VU 수가 아니라 iteration 시작률을 고정합니다.
            // "초당 몇 요청을 처리할 수 있는가"라는 목표가 있을 때 ramping-vus보다 적합합니다.
            executor: 'constant-arrival-rate',

            // 초당 POPULAR_RATE개의 iteration 시작을 목표로 합니다.
            // 서버가 느려지면 더 많은 VU가 필요합니다.
            rate: POPULAR_RATE,

            // timeUnit이 1s이면 rate는 초당 요청 시작 수로 해석됩니다.
            timeUnit: '1s',

            // duration은 이 요청률을 유지할 전체 시간입니다.
            duration: '2m',

            // k6가 요청률을 유지하기 위해 미리 확보하는 VU 수입니다.
            // 이 값이 너무 작으면 테스트 초반에 목표 RPS에 도달하지 못할 수 있습니다.
            preAllocatedVUs: 20,

            // 서버가 느려질 때 추가로 늘릴 수 있는 최대 VU 수입니다.
            // maxVUs에 자주 도달하면 서버가 느리거나 목표 rate가 너무 높은 것입니다.
            maxVUs: 100,
        },
    },
    thresholds: {
        // arrival-rate 테스트에서 실패율이 증가하면 목표 RPS를 감당하지 못하는 신호입니다.
        http_req_failed: ['rate<0.01'],

        // p95를 합격 기준으로 두고 p99는 결과에서 별도로 해석합니다.
        // 짧은 테스트에서 p99를 threshold로 두면 일시적인 outlier 하나가 전체 테스트를 실패시킬 수 있습니다.
        'http_req_duration{type:popular}': ['p(95)<500'],
    },
};

export default function () {
    // arrival-rate 모델에서는 iteration 자체가 일정한 속도로 시작되므로
    // 사용자의 think time을 표현하는 sleep을 의도적으로 넣지 않습니다.
    getPopularProducts();
}

export const handleSummary = createSummaryHandler('03-popular-arrival-rate');