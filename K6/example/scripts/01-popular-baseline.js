// k6/scripts/01-popular-baseline.js
// ------------------------------------------------------------
// 목적:
// - GET /products/popular 단일 API의 기준 성능을 측정합니다.
// - 인덱스 적용 전/후, 캐시 적용 전/후 비교의 기준선으로 사용합니다.
//
// 관찰할 것:
// - k6: http_req_duration p95, http_req_failed
// - Prometheus: shop_products_popular_query_duration_seconds
// - Loki: event=popular_products_queried durationMs
//
// 실행:
//   k6 run .\k6\scripts\01-popular-baseline.js
//
// 결과 이름 변경:
//   $env:RESULT_NAME='popular-before-index'
//   k6 run .\k6\scripts\01-popular-baseline.js
// ------------------------------------------------------------

import { sleep } from 'k6';
import { getPopularProducts } from '../lib/client.js';
import { randomSleepSeconds } from '../lib/config.js';
import { createSummaryHandler } from '../lib/summary.js';

export const options = {
    // summaryTrendStats는 k6 기본 콘솔 요약에 표시할 통계값입니다.
    // p99는 상위 1%의 가장 느린 요청을 보여주므로 tail latency 설명에 유용합니다.
    // 단, 로컬 짧은 테스트에서는 표본 수가 적어 p99가 흔들릴 수 있어 threshold는 p95 중심으로 둡니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    scenarios: {
        popular_baseline: {
            // ramping-vus는 동시에 행동하는 사용자 수를 단계적으로 늘립니다.
            // "사용자가 늘어날 때 응답 시간이 어떻게 변하는가"를 보기 좋습니다.
            executor: 'ramping-vus',

            // stages는 시간에 따라 목표 VU 수를 어떻게 바꿀지 정의합니다.
            // 30초 동안 5명까지 증가, 1분 동안 10명 유지, 30초 동안 0명으로 감소합니다.
            // 갑자기 큰 부하를 주기보다 ramp-up 구간을 두면 응답 시간 변화 추이를 관찰하기 쉽습니다.
            stages: [
                { duration: '30s', target: 5 },
                { duration: '1m', target: 10 },
                { duration: '30s', target: 0 },
            ],

            // gracefulRampDown은 VU를 줄일 때 진행 중인 iteration을 최대한 마무리하도록 기다리는 시간입니다.
            // 너무 짧으면 요청이 중간에 끊겨 테스트 결과가 거칠어질 수 있습니다.
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        // HTTP 실패율이 1% 이상이면 테스트 실패로 판단합니다.
        http_req_failed: ['rate<0.01'],

        // type=popular tag가 붙은 요청만 별도로 기준을 둡니다.
        // 전체 평균보다 핵심 API의 p95를 보는 것이 더 중요합니다.
        // p99는 결과 표에서 관찰하되, 짧은 로컬 실습에서는 outlier 영향이 커서 합격 기준에는 넣지 않습니다.
        'http_req_duration{type:popular}': ['p(95)<500'],
    },
};

export default function () {
    getPopularProducts();

    // 실제 사용자는 같은 API를 쉬지 않고 반복 호출하지 않습니다.
    // sleep을 넣어 사용자 행동 간격을 단순하게나마 모델링합니다.
    sleep(randomSleepSeconds());
}

export const handleSummary = createSummaryHandler('01-popular-baseline');